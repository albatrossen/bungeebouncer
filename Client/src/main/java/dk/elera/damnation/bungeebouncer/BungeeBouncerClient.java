/*
 *  Copyright 2014 Jes Andersen
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package dk.elera.damnation.bungeebouncer;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeBouncerClient extends JavaPlugin implements Listener, PluginMessageListener {
	
	private static final String CHANNEL_NAME = "BungeeBouncer";
	private static final int DEFAULT_VERIFICATION_TIMEOUT = 20;
	private static final SecureRandom random = new SecureRandom();
	private static final String UNABLE_TO_VERIFY_AUTHENTICATION_ON_BUNGEE = "Unable to verify authentication on Bungee";
	private static final String VERIFICATION_REPLY = "VerificationReply";
	HashMap<Player, Long> expectedNounces = new HashMap<Player, Long>();

	private Logger log;
		
	private int verificationTimeout = DEFAULT_VERIFICATION_TIMEOUT; //Ticks
	private Signature verifier;
	private void acceptPlayer(Player player) {
		log.info(player.getName() + " is authenticated on bungee");
		expectedNounces.remove(player);
	}
	
	@EventHandler
	public void denyCritical(AsyncPlayerChatEvent event){
		if (expectedNounces.containsKey(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void denyCritical(PlayerCommandPreprocessEvent event){
		if (expectedNounces.containsKey(event.getPlayer())){
			event.setCancelled(true);
			event.setMessage("/harmlesscommand"); //To prevent plugins that does not respect cancelled from doing stuff
		}
	}
	
	private void freezePlayer(final Player player) {
		log.info("Asking bungee for " + player.getName());
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			
			@Override
			public void run() {
				if(expectedNounces.containsKey(player)){
					log.severe("SECURITY WARNING: Bungee did not reply with verification on " + player.getName());
					player.kickPlayer(UNABLE_TO_VERIFY_AUTHENTICATION_ON_BUNGEE);
				}
			}
		}, verificationTimeout);
		expectedNounces.put(player, null);
	}
	
	private Signature getSignatureVerifier() {
		Signature verifier;
		try {
			verifier = Signature.getInstance("SHA256withRSA");
			verifier.initVerify(KeyManager.getPublic());
			return verifier;
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void onDisable() {
		for (Player player : expectedNounces.keySet()) {
			player.kickPlayer("Plugin reload while verifying user");
		}
	}

	@Override
	public void onEnable() {
		log = getLogger();
		verificationTimeout = getConfig().getInt("timeout", DEFAULT_VERIFICATION_TIMEOUT);
		getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, this);
        
		verifier = getSignatureVerifier();
        
        for (Player player : getServer().getOnlinePlayers()) {
        	log.log(Level.SEVERE, "SECURITY WARNING: Player online before plugin load: " + player.getName());
        	freezePlayer(player);
        	requestVerification(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		freezePlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event){
		expectedNounces.remove(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		expectedNounces.remove(event.getPlayer());
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player transport, byte[] bytes) {
        if (!channel.equals(CHANNEL_NAME)) {
            return;
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
			String type = in.readUTF();
			this.getLogger().info(type);
			if(VERIFICATION_REPLY.equals(type)){
				VerificationReply reply = new VerificationReply(in, verifier);
				onVerificationReply(reply);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error during parsing of packet - size was " + bytes.length, e);
		}
    }
	
	@EventHandler
	public void onRegisterChannel(PlayerRegisterChannelEvent event)
	{
		if(CHANNEL_NAME.equals(event.getChannel())){
			Player player = event.getPlayer();
			requestVerification(player);
		}
	}
	
	private void onVerificationReply(VerificationReply reply) {
		if (reply.isValid){
			Player player = getServer().getPlayer(reply.playerName);
			if (player != null){
				if(expectedNounces.get(player) == reply.nouce){
					acceptPlayer(player);
				} else {
					log.severe("SECURITY WARNING: Potential replay attack detected on " + reply.playerName);
					player.kickPlayer(UNABLE_TO_VERIFY_AUTHENTICATION_ON_BUNGEE);
				}
			}
		} else {
			log.severe("SECURITY WARNING: Invalid signature for " + reply.playerName);
		}
	}
	
	private void requestVerification(Player player) {
		long nounce = random.nextLong();
		expectedNounces.put(player, nounce);
		VerificationRequest request = new VerificationRequest(player.getName(), nounce );
		player.sendPluginMessage(this, CHANNEL_NAME, request.toBytes());
	}
}
