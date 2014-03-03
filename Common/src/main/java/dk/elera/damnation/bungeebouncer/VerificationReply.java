package dk.elera.damnation.bungeebouncer;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.security.SignatureException;

public class VerificationReply {

	private static ByteBuffer buffer = ByteBuffer.allocate(8);
	public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
	public transient final boolean isValid;
	public final long nouce;
	
	public final String playerName;

	private final byte[] signature;

    public VerificationReply(DataInput stream, Signature verifier) throws IOException {
		playerName = stream.readUTF();
		nouce = stream.readLong();
		int size = stream.readUnsignedByte();
		signature = new byte[size];
		stream.readFully(signature);
		isValid = Validate(verifier);
	}    

    public VerificationReply(String playerName, long nouce, Signature signer) {
		this.playerName = playerName;
		this.nouce = nouce;
		this.signature = getSignature(signer);
		this.isValid = true;
	}
	
	private byte[] getSignature(Signature signer){
		try {
			signer.update(playerName.getBytes());
			signer.update(longToBytes(nouce));
			return signer.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public byte[] toBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
 		return stream.toByteArray();
	}

	private boolean Validate(Signature verifier) {
		try {
			verifier.update(playerName.getBytes());
			verifier.update(longToBytes(nouce));
			return verifier.verify(this.signature);
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void write(DataOutput stream) throws IOException
	{
		stream.writeUTF(VerificationReply.class.getSimpleName());
		stream.writeUTF(playerName);
		stream.writeLong(nouce);
		assert signature.length <= 255:"signature too long";
		stream.write(signature.length);
		stream.write(signature);
	}
}
