package dk.elera.damnation.bungeebouncer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeeBouncerServer extends Plugin implements Listener {
	private static final String CHANNEL_NAME = "BungeeBouncer";
	private static final String VERIFICATION_REQUEST = "VerificationRequest";
	private Signature signer;
	

	private Signature getSigner() {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(KeyManager.getPrivate());
			return signature;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
    
	public void onEnable() {
		signer = getSigner(); 
		
        this.getProxy().registerChannel(CHANNEL_NAME);
        this.getProxy().getPluginManager().registerListener(this, this);
    }

	@EventHandler
    public void onPluginMessage(PluginMessageEvent ev) {
        if (!ev.getTag().equals(CHANNEL_NAME)) {
            return;
        }
   
        if (!(ev.getSender() instanceof Server)) {
            return;
        }
        Server server = (Server) ev.getSender();
 
        ByteArrayInputStream stream = new ByteArrayInputStream(ev.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
			String message = in.readUTF();
			this.getLogger().info(message);
			if(VERIFICATION_REQUEST.equals(message)){
				VerificationRequest request = new VerificationRequest(in);
				VerificationReply reply = new VerificationReply(request.playerName, request.nouce, signer);
				System.out.println("Sending reply");
				server.sendData(CHANNEL_NAME, reply.toBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
