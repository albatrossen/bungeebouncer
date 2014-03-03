package dk.elera.damnation.bungeebouncer;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class VerificationRequest {

	public final long nouce;
	public final String playerName;
	
	public VerificationRequest(DataInput stream) throws IOException {
		playerName = stream.readUTF();
		nouce = stream.readLong();
	}

	public VerificationRequest(String playerName, long nouce) {
		this.playerName = playerName;
		this.nouce = nouce;
	}

	public byte[] toBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			Write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
 		return stream.toByteArray();
	}
	
	private void Write(DataOutput stream) throws IOException
	{
		stream.writeUTF(VerificationRequest.class.getSimpleName());
		stream.writeUTF(playerName);
		stream.writeLong(nouce);
	}
}
