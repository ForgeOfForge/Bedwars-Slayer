package forge.BedwarsKillTracker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraft.entity.player.EntityPlayer; // Import EntityPlayer from net.minecraft.entity.player
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent; // Import PlayerEvent from net.minecraftforge.event.entity.player
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.audio.PositionedSoundRecord;


@Mod(modid = BedwarsKillTracker.MODID, version = BedwarsKillTracker.VERSION)
public class BedwarsKillTracker {
    public static final String MODID = "BedwarsKillTracker";
    public static final String VERSION = "1.0";

    private int playerOneKills;
    private int playerTwoKills;
    private int playerThreeKills;
    private int playerFourKills;
    private int actionBarTicks = 0;
    private final int maxActionBarTicks = 100000; // Adjust as needed

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private long lastSoundTime = 0L;
    private static final long COOLDOWN_DURATION = 2500L; // 2 seconds in milliseconds
    private List<String> queuedSounds = new ArrayList<String>();
    private boolean isCoolingDown = false;
    
    private void playSoundWithCooldown(String soundName) {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastSoundTime >= COOLDOWN_DURATION) {
            // If not in cooldown, play the sound immediately and set cooldown
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(MODID, soundName), 1.0F));
            lastSoundTime = currentTime;
            isCoolingDown = true;
        } else if (!queuedSounds.contains(soundName) && isCoolingDown) {
            // If in cooldown and sound is not in queue, enqueue the sound for later
            queuedSounds.add(soundName);
        }
    }

    private String activePlayerUsername = Minecraft.getMinecraft().getSession().getUsername();
    
    // Fetch these from party members (don't add if duplicate exists, and display all four's kills)
    private String playerOne = activePlayerUsername;
    private String playerTwo = "q6wUC*eDL6$iW%Mb";
    private String playerThree = "q6wUC*eDL6$iW%Mb";
    private String playerFour = "q6wUC*eDL6$iW%Mb";
    
    private void processQueuedSounds() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastSoundTime >= COOLDOWN_DURATION) {
            isCoolingDown = false;
        }
        
        if (!queuedSounds.isEmpty() && !isCoolingDown) {
            String soundName = queuedSounds.remove(0);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation(MODID, soundName), 1.0F));
            lastSoundTime = currentTime;
            isCoolingDown = true; // Set cooling down after processing one sound
        }
    }
    
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) { 	
        String msg = event.message.getUnformattedText();

		String[] words = msg.split(" ");
		 // Check for new players joining your own party
		 if (msg.contains(" joined the party") && !msg.contains("]")) {
		     if (words.length >= 2) {
		         String usernameJoined = words[0];
		         Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Trying to add player: " + usernameJoined));
		     
		         // Add to player list 
		         if (!playerOne.equals(usernameJoined) && !playerTwo.equals(usernameJoined) && !playerThree.equals(usernameJoined) && !playerFour.equals(usernameJoined)) {
		             if (playerTwo.equals("q6wUC*eDL6$iW%Mb")) {
		                 Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Two: " + usernameJoined));
		                 playerTwo = usernameJoined;
		             } else if (playerThree.equals("q6wUC*eDL6$iW%Mb")) {
		                 Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Three: " + usernameJoined));
		                 playerThree = usernameJoined;
		             } else if (playerFour.equals("q6wUC*eDL6$iW%Mb")) {
		                 Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Four: " + usernameJoined));
		                 playerFour = usernameJoined;
		             }
		         }
		     }
		 }
		 // Check for joining other player's party - save party leader as player 2
		 if (msg.contains("You have joined ") && msg.contains(" party!") && !msg.contains(":")) {
            String usernameJoinedIncomplete = words[words.length - 2];
            String usernameJoined = usernameJoinedIncomplete.replaceAll("'s\\b", "");
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Trying to add player: " + usernameJoined));

            // Add to player list
            if (!playerOne.equals(usernameJoined) && !playerTwo.equals(usernameJoined) && !playerThree.equals(usernameJoined) && !playerFour.equals(usernameJoined)) {
                if (playerTwo.equals("q6wUC*eDL6$iW%Mb")) {
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Two: " + usernameJoined));
                    playerTwo = usernameJoined;
                } else if (playerThree.equals("q6wUC*eDL6$iW%Mb")) {
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Three: " + usernameJoined));
                    playerThree = usernameJoined;
                } else if (playerFour.equals("q6wUC*eDL6$iW%Mb")) {
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Four: " + usernameJoined));
                    playerFour = usernameJoined;
                }
            }
		 }



		// Check for joining other player's party - save first 2 other players as players 3 & 4. Others are ignored
		if (msg.contains("You'll be partying with: ") && !msg.contains("[")) {
		    if (words.length >= 5) {
		        String usernameJoined = words[4]; // Extract the username directly
		        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Trying to add player: " + usernameJoined));

		        // Add to player list
		        if (!playerOne.equals(usernameJoined) && !playerTwo.equals(usernameJoined) && !playerThree.equals(usernameJoined) && !playerFour.equals(usernameJoined)) {
		            if (playerTwo.equals("q6wUC*eDL6$iW%Mb")) {
		                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Two: " + usernameJoined));
		                playerTwo = usernameJoined;
		            } else if (playerThree.equals("q6wUC*eDL6$iW%Mb")) {
		                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Three: " + usernameJoined));
		                playerThree = usernameJoined;
		            } else if (playerFour.equals("q6wUC*eDL6$iW%Mb")) {
		                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Four: " + usernameJoined));
		                playerFour = usernameJoined;
		            }
		        }
		    }
		}
		// Reset all party member info with /party list
		// You can also just leave hypixel and re-log to clear party info
		String msgSansFluff = msg.replace("Party Members: ", "").replace("[VIP] ", "").replace("[VIP+] ", "").replace("[MVP] ", "").replace("[MVP+] ", "").replace("[MVP++] ", "");
		String[] wordsSansFluff = msgSansFluff.split(" ");
		// Rebuild player list
		if (msg.contains("Party Leader: ") && !(msg.charAt(0) == '[')) {
		    playerOne = activePlayerUsername;
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player One: " + words[words.length - 2]));
			playerOne = words[words.length - 2];
		}
		
		if (msg.contains("Party Members: ") && !(msg.charAt(0) == '[')) {
			if (words.length >= 2) {
			    playerTwo = "q6wUC*eDL6$iW%Mb";
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Two: " + wordsSansFluff[0]));
				playerTwo = wordsSansFluff[0];
			}
			if (words.length >= 3) {
			    playerThree = "q6wUC*eDL6$iW%Mb";
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Three: " + wordsSansFluff[2]));
				playerThree = wordsSansFluff[2];
			}
			if (words.length >= 4) {
			    playerFour = "q6wUC*eDL6$iW%Mb";
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Adding Player Four: " + wordsSansFluff[4]));
				playerFour = wordsSansFluff[4];
			}
		}

        // Sound for game start
        if (msg.contains("The game starts in 1 second!") && !msg.contains("]")){
        	String soundName = "slayer";
        	playSoundWithCooldown(soundName);
            // Reset counters
            playerOneKills = 0;
            playerTwoKills = 0;
            playerThreeKills = 0;
            playerFourKills = 0;
            actionBarTicks = maxActionBarTicks;
        }
        	
        if ((msg.contains(playerOne) || msg.contains(playerTwo) || msg.contains(playerThree) || msg.contains(playerFour)) && !msg.contains("]")) {
            // Check which player the kill message is for
            boolean isPlayerOneKill = msg.contains("by " + playerOne);
            boolean isPlayerTwoKill = msg.contains("by " + playerTwo);
            boolean isPlayerThreeKill = msg.contains("by " + playerThree);
            boolean isPlayerFourKill = msg.contains("by " + playerFour);

            if (isPlayerOneKill || isPlayerTwoKill || isPlayerThreeKill || isPlayerFourKill) {
                String soundName;
                if (isPlayerOneKill) {
                    playerOneKills++;
                    int personalKillStreak = playerOneKills;
                    playKillSound(personalKillStreak);
                    calculateLeaderboard(playerOne, personalKillStreak);
                }
                if (isPlayerTwoKill) {
                    playerTwoKills++;
                    int personalKillStreak = playerTwoKills;
                    playKillSound(personalKillStreak);
                    calculateLeaderboard(playerTwo, personalKillStreak);
                }
                if (isPlayerThreeKill) {
                    playerThreeKills++;
                    int personalKillStreak = playerThreeKills;
                    playKillSound(personalKillStreak);
                    calculateLeaderboard(playerThree, personalKillStreak);
                }
                if (isPlayerFourKill) {
                    playerFourKills++;
                    int personalKillStreak = playerFourKills;
                    playKillSound(personalKillStreak);
                    calculateLeaderboard(playerFour, personalKillStreak);
                }
                // This runs for all players on any kill
                actionBarTicks = maxActionBarTicks;
            }
        }

    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        updateActionBar();
        processQueuedSounds(); // Process queued sounds
    }

    private void updateActionBar() {
        if (actionBarTicks > 0) {
            GuiIngame guiIngame = Minecraft.getMinecraft().ingameGUI;
            String actionBarText = EnumChatFormatting.GOLD + playerOne + " Kills: " + playerOneKills;
            if (!playerTwo.contains("q6wUC*eDL6$iW%Mb")){
            	actionBarText += " | " + playerTwo + " Kills: " + playerTwoKills;
            }
            if (!playerThree.contains("q6wUC*eDL6$iW%Mb")){
            	actionBarText += " | " + playerThree + " Kills: " + playerThreeKills;
            }
            if (!playerFour.contains("q6wUC*eDL6$iW%Mb")){
            	actionBarText += " | " + playerFour + " Kills: " + playerFourKills;
            }
            guiIngame.setRecordPlaying(actionBarText, false);
            actionBarTicks--;
        }
    }
    //---LEADERBOARD
    private String previousLeader = "well... no one";
    private String currentLeader = "";

    private void calculateLeaderboard(String playerName, int personalKillStreak) {
        int[] killCounts = {playerOneKills, playerTwoKills, playerThreeKills, playerFourKills};
        String[] playerNames = {playerOne, playerTwo, playerThree, playerFour};

        // Find the current leader's index
        int currentLeaderIndex = -1; // Initialize to -1 to indicate no leader yet
        int currentLeaderKills = 0; // Initialize to 0

        for (int i = 0; i < killCounts.length; i++) {
            if (killCounts[i] > currentLeaderKills) {
                currentLeaderIndex = i;
                currentLeaderKills = killCounts[i];
            }
        }

        // Determine if there is a clear leader
        boolean hasClearLeader = true;
        for (int i = 0; i < killCounts.length; i++) {
            if (i != currentLeaderIndex && killCounts[i] == currentLeaderKills) {
                hasClearLeader = false;
                break;
            }
        }

        // Store the current leader's name
        String newLeader = (currentLeaderIndex != -1) ? playerNames[currentLeaderIndex] : "";

        // Notify of new leader and previous leader
        if (hasClearLeader) {
            if (playerName.equals(newLeader)) {
                if (!playerName.equals(previousLeader)) {
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GREEN + newLeader + " is now the leader with " + currentLeaderKills + " kills! Overtaking " + previousLeader));
                    if (activePlayerUsername.equals(newLeader)){
                    	String soundName = "gained_lead";
                        playSoundWithCooldown(soundName);
                    } else if (activePlayerUsername.equals(previousLeader)){
                    	String soundName = "lost_lead";
                        playSoundWithCooldown(soundName);
                    }
                    previousLeader = newLeader; // Update previous leader when a new leader emerges
                }
            } else if (playerName.equals(previousLeader) && personalKillStreak < currentLeaderKills) {
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You are no longer the leader. " + newLeader + " is now leading with " + currentLeaderKills + " kills!"));
            }
        } else if (playerName.equals(previousLeader) && personalKillStreak < currentLeaderKills) {
            // Check if the previous leader has regained the lead
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GREEN + "You are now the leader with " + currentLeaderKills + " kills!"));
            previousLeader = playerName; // Update previous leader when they regain the lead
        }
    }


    //---LEADERBOARD




    private void playKillSound(int personalKillStreak) {
        String soundName;

        switch (personalKillStreak) {
            case 1:
                soundName = "one_kill";
                break;
            case 2:
                soundName = "two_kill";
                break;
            case 3:
                soundName = "three_kill";
                break;
            case 4:
                soundName = "four_kill";
                break;
            case 5:
                soundName = "five_kill";
                break;
            case 6:
                soundName = "six_kill";
                break;
            case 7:
                soundName = "seven_kill";
                break;
            case 8:
                soundName = "eight_kill";
                break;
            case 9:
                soundName = "nine_kill";
                break;
            case 10:
                soundName = "ten_kill";
                break;
            case 11:
                soundName = "eleven_kill";
                break;
            case 12:
                soundName = "twelve_kill";
                break;
            case 13:
                soundName = "thirteen_kill";
                break;
            case 14:
                soundName = "fourteen_kill";
                break;
            case 15:
                soundName = "fifteen_kill";
                break;
            case 16:
                soundName = "sixteen_kill";
                break;
            case 17:
                soundName = "seventeen_kill";
                break;
            case 18:
                soundName = "eighteen_kill";
                break;
            case 19:
                soundName = "nineteen_kill";
                break;
            case 20:
                soundName = "twenty_kill";
                break;
            case 21:
                soundName = "twentyone_kill";
                break;
            case 22:
                soundName = "twentytwo_kill";
                break;
            case 23:
                soundName = "twentythree_kill";
                break;
            case 24:
                soundName = "twentyfour_kill";
                break;
            default:
                soundName = "twentyfive_kill";
                break;
        }
        playSoundWithCooldown(soundName);
    }

    }
