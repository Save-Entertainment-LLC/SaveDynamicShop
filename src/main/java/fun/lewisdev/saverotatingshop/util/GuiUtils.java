package fun.lewisdev.saverotatingshop.util;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import fun.lewisdev.saverotatingshop.shop.BuyShop;
import fun.lewisdev.saverotatingshop.shop.ShopManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GuiUtils {

    public static void setSlot(BaseGui gui, GuiItem guiItem, ConfigurationSection section) {
        if (section.getBoolean("fill_border")) {
            gui.getFiller().fillBorder(guiItem);
        }

        if (section.contains("slot")) {
            int slot = section.getInt("slot");
            if (slot == -1) {
                gui.getFiller().fill(guiItem);
            } else {
                gui.setItem(slot, guiItem);
            }
        } else if (section.contains("slots")) {
            for (String slot : section.getStringList("slots")) {
                if (slot.contains("-")) {
                    String[] args = slot.split("-");
                    for (int i = Integer.parseInt(args[0]); i < Integer.parseInt(args[1]) + 1; ++i)
                        gui.setItem(i, guiItem);
                } else {
                    gui.setItem(Integer.parseInt(slot), guiItem);
                }

            }
        }
    }

    public static void setFillerItems(BaseGui gui, ConfigurationSection section, ShopManager shopManager, Player player, BuyShop shop) {
        if (section != null) {
            for (String entry : section.getKeys(false)) {
                ConfigurationSection itemSection = section.getConfigurationSection(entry);

                ItemStackBuilder builder = ItemStackBuilder.getItemStack(itemSection);
                if(shop != null) {
                    ItemStack clone = builder.build();
                    if (clone.hasItemMeta() && clone.getItemMeta().hasLore()) {
                        List<String> newLore = new ArrayList<>();
                        for (String line : clone.getItemMeta().getLore()) {

                            double balance = shopManager.getPlugin().getEconomy().getBalance(player);
                            line = line.replace("{BALANCE}", TextUtil.format(balance))
                                    .replace("{REFRESH_TIME}", TextUtil.formatTime((shop.getRefreshTime() - System.currentTimeMillis()) / 1000));

                            newLore.add(line);
                        }

                        builder.withLore(newLore);
                    }
                }

                GuiItem guiItem = new GuiItem(builder.build());
                if(itemSection.getBoolean("open_sell_gui")) {
                    guiItem.setAction(event -> shopManager.openSellGui(player));
                }

                setSlot(gui, guiItem, itemSection);
            }
        }
    }

}
