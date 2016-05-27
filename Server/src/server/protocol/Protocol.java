package server.protocol;

import java.util.Arrays;

import server.protocol.message.Message;
import server.protocol.message.PutMessage;
import server.protocol.message.StoredMessage;


public class Protocol {
	public static final String CRLF = "\r\n";
	
	public static Message parseMessage(byte[] data) {
		byte[] pattern = (Protocol.CRLF + Protocol.CRLF).getBytes();
		int index = findHeaderLength(data, pattern);
		String[] header = new String(data, 0, index).split("\\s+");
		byte[] body = null;
		if (index < data.length)
			body = Arrays.copyOfRange(data, index + pattern.length, data.length);

		// returns the message in case of success or null in case of error
		Message msg = getMessageType(header, body);
		return msg;
	}

	/**
	 * 
	 * @param data
	 * @return The length of the header without the last 2 CRLFs.
	 */
	private static int findHeaderLength(byte[] data, byte[] pattern) {
		for (int i = 0; i <= data.length - pattern.length; i++) {
			if (Arrays.equals(pattern, Arrays.copyOfRange(data, i, i + pattern.length)))
			{
				return i;
			}
		}
		return data.length;
	}

	private static Message getMessageType(String[] header, byte[] body) {
		switch (header[0]) {
		case "PUT":
			return parsePUT(header, body);
		case "STORED":
			return parseSTORED(header);
		default:
			break;
		}

		return null;
	}

	private static PutMessage parsePUT(String header[], byte[] body) {
		if (header.length != 2)
			return null;

		int fileId = new Integer(header[1]);
		
		return new PutMessage(fileId, body);
	}

	private static StoredMessage parseSTORED(String[] header) {
		if (header.length != 2)
			return null;

		int fileId = new Integer(header[1]);

		return new StoredMessage(fileId);
	}


}
