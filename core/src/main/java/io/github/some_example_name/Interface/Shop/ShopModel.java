package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import java.util.ArrayList;
import java.util.List;

public class ShopModel {
    public static class ShopItem {
        public String id, name, type, iconPath;
        public int price;
        public String trait, esmeraldaQuote;
        public JsonValue stats; // mantém como JsonValue para acesso flexível

        public ShopItem(String id, String name, int price, String type, String iconPath,
                JsonValue stats, String trait, String esmeraldaQuote) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.type = type;
            this.iconPath = iconPath;
            this.stats = stats;
            this.trait = trait;
            this.esmeraldaQuote = esmeraldaQuote;
        }
    }

    private List<ShopItem> allItems = new ArrayList<>();
    private List<ShopItem> filteredItems = new ArrayList<>();
    private String currentCategory = "weapon";
    private Robertinhoo player;
    private boolean purchaseMade = false;

    public ShopModel(Robertinhoo player) {
        this.player = player;
        loadItemsFromJson();
        filterItemsByCategory();
    }

    private void loadItemsFromJson() {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("data/itens.json"));
            for (JsonValue entry : root) {
                String id = entry.getString("id");
                String name = entry.getString("name");
                int price = entry.getInt("price");
                String type = entry.getString("type");
                String iconPath = entry.getString("iconPath");

                // Lê os campos adicionais
                JsonValue stats = entry.get("stats"); // pode ser null se não existir
                String trait = entry.getString("trait", "");
                String esmeraldaQuote = entry.getString("esmeraldaQuote", "");

                allItems.add(new ShopItem(id, name, price, type, iconPath, stats, trait, esmeraldaQuote));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback com item padrão (sem os extras)
            allItems.add(new ShopItem("pistol", "Pistola", 100, "weapon",
                    "ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png", null, "", ""));
        }
    }

    public void filterItemsByCategory() {
        filteredItems.clear();
        for (ShopItem item : allItems) {
            if (item.type.equals(currentCategory)) {
                filteredItems.add(item);
            }
        }
        // Preenche espaços vazios na grade
        while (filteredItems.size() < 6) {
            filteredItems.add(null);
        }
    }

    public List<ShopItem> getFilteredItems() {
        return filteredItems;
    }

    public String getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(String category) {
        this.currentCategory = category;
        filterItemsByCategory();
    }

    // Em ShopModel.java
    public boolean buyItem(ShopItem item) {
        if (item == null)
            return false;
        if (item.price > 0 && !player.getSoulManager().spendSouls(item.price)) {
            purchaseMade = false;
            return false;
        }
        purchaseMade = true;

        // Lógica de adicionar ao inventário
        if (item.type.equals("weapon") && item.id.equals("pistol")) {
            Pistol pistol = new Pistol(player.getMap(), 0, 0, player.getInventory());
            player.getInventory().addWeapon(pistol);
        }

        allItems.remove(item);

        filterItemsByCategory();
        return true;
    }

    public ShopItem getItemAt(int index) {
        if (index >= 0 && index < filteredItems.size()) {
            return filteredItems.get(index);
        }
        return null;
    }

    public boolean buyItemAt(int index) {
        ShopItem item = getItemAt(index);
        return item != null && buyItem(item);
    }

    public boolean wasPurchaseMade() {
        return purchaseMade;
    }

    public void resetPurchaseFlag() {
        purchaseMade = false;
    }
}