package org.aib.av;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface IVideoStreamProducer {
	boolean start(String pipeline, Consumer<ByteBuffer> callback);
	boolean stop();
}
