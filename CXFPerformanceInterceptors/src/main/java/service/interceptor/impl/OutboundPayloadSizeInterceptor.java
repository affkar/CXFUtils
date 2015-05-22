package service.interceptor.impl;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import service.interceptor.api.OutboundPayloadSizePhaseInterceptor;

public class OutboundPayloadSizeInterceptor extends
		AbstractPhaseInterceptor<Message> implements
		OutboundPayloadSizePhaseInterceptor<Message> {

	protected int limit = 100 * 1024;

	public OutboundPayloadSizeInterceptor() {
		super(Phase.PRE_STREAM);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		final OutputStream os = message.getContent(OutputStream.class);
		final Writer iowriter = message.getContent(Writer.class);
		if (os == null && iowriter == null) {
			return;
		}
		if (os != null) {
			final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(
					os);
			message.setContent(OutputStream.class, newOut);
			newOut.registerCallback(new OutPayloadSizeCallback(message, os));
		} else {
			message.setContent(Writer.class,
					new PayloadSizeExtractAndWriteWriter(message, iowriter));
		}
	}

	class OutPayloadSizeCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;

		public OutPayloadSizeCallback(final Message msg, final OutputStream os) {
			this.message = msg;
			this.origStream = os;
		}

		public void onFlush(CachedOutputStream cos) {

		}

		public void onClose(CachedOutputStream cos) {
			try {
				System.out.println("OutPayloadSizeCallback Available Output bytes:: "
						+ cos.getInputStream().available());
				message.getExchange().put(OUTBOUND_PAYLOAD_SIZE,
						cos.getInputStream().available());
			} catch (Exception ex) {
				// ignore
			}

			try {
				// empty out the cache
				cos.lockOutputStream();
				cos.resetOut(null, false);
			} catch (Exception ex) {
				// ignore
			}
			message.setContent(OutputStream.class, origStream);
		}
	}

	private class PayloadSizeExtractAndWriteWriter extends FilterWriter {
		int count;
		Message message;

		public PayloadSizeExtractAndWriteWriter(Message message, Writer writer) {
			super(writer);
			this.message = message;

		}

		public void write(int c) throws IOException {
			super.write(c);
			count++;
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			super.write(cbuf, off, len);
			count += len;
		}

		public void write(String str, int off, int len) throws IOException {
			super.write(str, off, len);
			count += len;
		}

		public void close() throws IOException {
			System.out.println("Outbound count from PayloadSizeExtractAndWriteWriter is "+count);
			message.getExchange().put(OUTBOUND_PAYLOAD_SIZE, count);
			message.setContent(Writer.class, out);
			super.close();
		}
	}

}
