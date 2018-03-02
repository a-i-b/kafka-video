package org.aib.av;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.StateChangeReturn;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.freedesktop.gstreamer.lowlevel.GstEventAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VideoStreamConsumer implements IVideoStreamConsumer {

	static Logger logger = LoggerFactory.getLogger(VideoStreamConsumer.class);

	AppSrc appsrc;

	private Pipeline pipe;

	public VideoStreamConsumer() {
		Gst.init("Video streaming service", new String[] { "--gst-debug=2", "--gst-debug-no-color" });
	}

	@Override
	public void post(ByteBuffer data) {
		if (appsrc == null)
			return;

		logger.debug("Posted bytes: " + data.remaining());

		Buffer buffer = new Buffer(data.remaining());
		ByteBuffer bb = buffer.map(true);
		bb.put(data);
		appsrc.pushBuffer(buffer);
	}

	@Override
	public boolean start(String pipeline, Consumer<ByteBuffer> onOutputDataReady) {
		try {

			Bin readBin = Bin.launch(pipeline, true);
			readBin.setName("readBin");

			final AppSink appsink = new AppSink("appsink");
			appsink.set("emit-signals", true);
			appsink.connect(new AppSink.NEW_SAMPLE() {
				public FlowReturn newSample(AppSink elem) {
					Sample sample = null;
					Buffer buffer = null;
					try {
						sample = elem.pullSample();
						buffer = sample.getBuffer();
						ByteBuffer bb = buffer.map(false);
						if (bb != null) {
							onOutputDataReady.accept(bb);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (buffer != null)
							buffer.unmap();
						if (sample != null)
							sample.dispose();
					}
					return FlowReturn.OK;
				}
			});

			pipe = new Pipeline("consumer");
			pipe.addMany(readBin, appsink);
			appsrc = (AppSrc) pipe.getElementByName("appsrc");

			appsrc.set("emit-signals", true);
			appsrc.connect(new AppSrc.ENOUGH_DATA() {
				public void enoughData(AppSrc elem) {
					System.out.println("ENOUGH_DATA: Element=" + elem.getNativeAddress());
				}
			});

			logger.error("appsrc.getMaxBytes() :" + appsrc.getMaxBytes());
			
			Pipeline.linkMany(readBin, appsink);
			StateChangeReturn ret = pipe.play();
			if (ret == StateChangeReturn.ASYNC || ret == StateChangeReturn.SUCCESS) {
				return true;
			}

			logger.error("Pipeline play() returned " + ret.name());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	@Override
	public boolean startWithoutAppSink(String pipeline) {
		try {
			Bin readBin = Bin.launch(pipeline, true);
			readBin.setName("readBin");
			pipe = new Pipeline("consumerWithoutAppSink");
			pipe.add(readBin);

			appsrc = (AppSrc) pipe.getElementByName("appsrc");

			appsrc.set("emit-signals", true);
			appsrc.connect(new AppSrc.ENOUGH_DATA() {
				public void enoughData(AppSrc elem) {
					System.out.println("ENOUGH_DATA: Element=" + elem.getNativeAddress());
				}
			});
			
//			Pipeline.linkMany(readBin);
			StateChangeReturn ret = pipe.play();
			if (ret == StateChangeReturn.ASYNC || ret == StateChangeReturn.SUCCESS) {
				return true;
			}
	
			logger.error("Pipeline play() returnd " + ret.name());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	@Override
	public boolean sendEos() {
		if (pipe == null)
			return true;
		try {
			pipe.sendEvent(GstEventAPI.GSTEVENT_API.gst_event_new_eos());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
