package org.aib.av;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IVideoStreamConsumer {
	void post(ByteBuffer buffer);
	boolean start(String pipeline, Consumer<ByteBuffer> onOutputDataReady);
	boolean startWithoutAppSink(String pipeline);
	boolean sendEos();
}
