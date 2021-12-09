package de.theholyexception.holyapi.integrations.spigot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemBuilder {
	
	private ItemStack item;
	private ItemMeta meta;
	private List<String> lore;
	
	public ItemBuilder(Material material) {
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();
		lore = new ArrayList<>();
	}
	
	public ItemBuilder(Material material, int amount) {
		this.item = new ItemStack(material, amount);
		this.meta = item.getItemMeta();
		lore = new ArrayList<>();
	}
	
	public ItemBuilder(ItemStack item) {
		this.item = new ItemStack(item);
		this.meta = item.getItemMeta();
		lore = new ArrayList<>();
	}
	
	public ItemBuilder setDisplayName(String name) {
		meta.setDisplayName(name);
		return this;
	}
	
	public ItemBuilder addLoreLines(String... lines) {
		lore.addAll(Arrays.asList(lines));
		return this;
	}
	
	public ItemBuilder setSkullTexture(String texture) {
		GameProfile vplayer = new GameProfile(UUID.randomUUID(), null);
		vplayer.getProperties().put("textures", new Property("textures", texture));
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		Field profileField = null;

		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, vplayer);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

		item.setItemMeta(meta);
		return this;
	}
	
    public ItemBuilder addEnchantment(Enchantment ench, int lvl) {
        meta.addEnchant(ench, lvl, true);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        meta.addItemFlags(flag);
        return this;
    }
	
	public ItemStack build() {
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}
