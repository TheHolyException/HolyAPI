package de.theholyexception.holyapi.integrations;

import org.fusesource.jansi.Ansi.Color;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotUtils {
	
	
	public static class MessageBuilder {
		
		TextComponent component;
		
		public MessageBuilder() {
			component = new TextComponent();
		}
		
		public MessageBuilder append(String s) {
			component.addExtra(s);
			return this;
		}
		
		public MessageBuilder append(String s, ChatColor color) {
			TextComponent c = new TextComponent(s);
			c.setColor(color);
			component.addExtra(c);
			return this;
		}
		
		public MessageBuilder appendHoverText(TextComponent s, TextComponent hoverText) {
			s.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			component.addExtra(s);
			return this;
		}
		
		public MessageBuilder appendHoverText(String s, String hoverText) {
			appendHoverText(new TextComponent(s), new TextComponent(hoverText));
			return this;
		}
		
		public MessageBuilder appendHoverText(String s, TextComponent hoverText) {
			appendHoverText(new TextComponent(s), hoverText);
			return this;
		}
		
		public MessageBuilder appendHoverText(TextComponent s, String hoverText) {
			appendHoverText(s, new TextComponent(hoverText));
			return this;
		}
		
		public MessageBuilder appendClickRC(String s, String executeCommand) {
			TextComponent c = new TextComponent();
			c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, executeCommand));;
			component.addExtra(c);
			return this;
		}
		
		public MessageBuilder parseAppend(String s) {
			ChatColor lastColor = ChatColor.WHITE;
			boolean containsParagraph = false;
			boolean bold = false;
			boolean italic = false;
			boolean underline = false;
			boolean strikethrough = false;
			boolean shouldChange = false;
			TextComponent baseComponent = new TextComponent();
			TextComponent componentA = new TextComponent();
			StringBuilder builder = new StringBuilder();
			
			for (char c : s.toCharArray()) {
				if (containsParagraph) {
					ChatColor color = getColor(c);
					if (color != null) {
						lastColor = color;
						bold = false;
		        		italic = false;
		        		underline = false;
		        		strikethrough = false;
					} else {
						switch (c) {
						case 'l': bold = true; break;
						case 'o': italic = true; break;
						case 'u': underline = true; break;
						case 'm': strikethrough = true; break;
						default:break;
						}
					}
					containsParagraph = false;
					shouldChange = true;
					continue;
				}
				
				if (shouldChange) {
					componentA.setText(builder.toString());
					baseComponent.addExtra(componentA);
					componentA = new TextComponent();
					
					componentA.setColor(lastColor);
					componentA.setBold(bold);
					componentA.setItalic(italic);
					componentA.setUnderlined(underline);
					componentA.setStrikethrough(strikethrough);
					
					builder = new StringBuilder();
					shouldChange = false;
				}
			}
			
			if (builder.length() > 0) {
				componentA.setText(builder.toString());
				baseComponent.addExtra(componentA);
				component.addExtra(baseComponent);
			}
			return this;
		}
		
		public TextComponent create() {
			return component;
		}

		private ChatColor getColor(char c) {
	    	switch (c) {
			case '0': return ChatColor.BLACK; 
			case '1': return ChatColor.DARK_BLUE; 
			case '2': return ChatColor.DARK_GREEN; 
			case '3': return ChatColor.DARK_AQUA; 
			case '4': return ChatColor.DARK_RED; 
			case '5': return ChatColor.DARK_PURPLE; 
			case '6': return ChatColor.GOLD; 
			case '7': return ChatColor.GRAY; 
			case '8': return ChatColor.DARK_GRAY; 
			case '9': return ChatColor.BLUE; 
			case 'a': return ChatColor.GREEN; 
			case 'b': return ChatColor.AQUA; 
			case 'c': return ChatColor.RED; 
			case 'd': return ChatColor.LIGHT_PURPLE; 
			case 'e': return ChatColor.YELLOW; 
			case 'f': return ChatColor.WHITE;
			default:
				break;
			}
	    	return null;
	    }
		
	}

}
