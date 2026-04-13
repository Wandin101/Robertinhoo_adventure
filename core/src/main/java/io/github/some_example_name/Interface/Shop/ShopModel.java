package io.github.some_example_name.Interface.Shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Inventory.Item;
import java.util.ArrayList;
import java.util.List;

public class ShopModel {
    public enum PurchaseError {
        NONE,
        INSUFFICIENT_SOULS,
        INVENTORY_FULL
    }

    public static class ShopItem {
        public String id, name, type, iconPath;
        public int price;
        public String trait, esmeraldaQuote;
        public JsonValue stats;
        public int gridWidth, gridHeight;

        public ShopItem(String id, String name, int price, String type, String iconPath,
                JsonValue stats, String trait, String esmeraldaQuote,
                int gridWidth, int gridHeight) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.type = type;
            this.iconPath = iconPath;
            this.stats = stats;
            this.trait = trait;
            this.esmeraldaQuote = esmeraldaQuote;
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
        }
    }

    private List<ShopItem> allItems = new ArrayList<>();
    private List<ShopItem> filteredItems = new ArrayList<>();
    private String currentCategory = "weapon";
    private Robertinhoo player;
    private boolean purchaseMade = false;
    private PurchaseError lastError = PurchaseError.NONE;

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
                int gridWidth = entry.getInt("gridWidth", 1);
                int gridHeight = entry.getInt("gridHeight", 1);
                JsonValue stats = entry.get("stats");
                String trait = entry.getString("trait", "");
                String esmeraldaQuote = entry.getString("esmeraldaQuote", "");

                allItems.add(new ShopItem(id, name, price, type, iconPath, stats, trait, esmeraldaQuote, gridWidth,
                        gridHeight));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback com item padrão
            allItems.add(new ShopItem("pistol", "Pistola", 100, "weapon",
                    "ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png", null, "", "", 3, 2));
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

    public PurchaseError getLastError() {
        return lastError;
    }

    // Verifica se há espaço no inventário para um item com as dimensões
    // especificadas
    private boolean hasInventorySpace(int gridWidth, int gridHeight) {
        return player.getInventory().canFit(gridWidth, gridHeight);
    }

    // Cria o item real (a ser adicionado ao inventário)
    private Item createRealItem(ShopItem item) {
        if (item.type.equals("weapon")) {
            if (item.id.equals("pistol")) {
                return new Pistol(player.getMap(), 0, 0, player.getInventory());
            }
            // outros tipos de arma...
        } else if (item.type.equals("ammo")) {
            // criar munição
        } else if (item.type.equals("armor")) {
            // criar armadura
        }
        return null;
    }

    public boolean buyItem(ShopItem item) {
        if (item == null) {
            lastError = PurchaseError.NONE;
            return false;
        }

        // 1. Verifica almas
        if (item.price > 0 && !player.getSoulManager().spendSouls(item.price)) {
            lastError = PurchaseError.INSUFFICIENT_SOULS;
            purchaseMade = false;
            return false;
        }

        // 2. Verifica espaço no inventário
        if (!hasInventorySpace(item.gridWidth, item.gridHeight)) {
            if (item.price > 0) {
                player.getSoulManager().addSouls(item.price); // devolve almas
            }
            lastError = PurchaseError.INVENTORY_FULL;
            purchaseMade = false;
            return false;
        }

        // 3. Cria o item real e adiciona
        Item realItem = createRealItem(item);
        if (realItem == null || !player.getInventory().addItem(realItem)) {
            // falha inesperada
            if (item.price > 0) {
                player.getSoulManager().addSouls(item.price);
            }
            lastError = PurchaseError.INVENTORY_FULL;
            purchaseMade = false;
            return false;
        }

        // 4. Sucesso
        purchaseMade = true;
        allItems.remove(item);
        filterItemsByCategory();
        lastError = PurchaseError.NONE;
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