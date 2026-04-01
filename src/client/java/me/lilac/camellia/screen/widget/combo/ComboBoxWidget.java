package me.lilac.camellia.screen.widget.combo;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

@Environment(EnvType.CLIENT)
public class ComboBoxWidget extends AbstractWidget {

    private final EditBox searchBox;
    private final List<Entry> entries;
    private final List<Entry> filteredEntries;
    private String value;
    private boolean showEntries;
    private int scrollOffset;

    public ComboBoxWidget(int x, int y, int width, int height, String value, List<String> entries) {
        super(x, y, width, height, CommonComponents.EMPTY);

        this.entries = new ArrayList<>();
        this.filteredEntries = new ArrayList<>();
        this.value = value;
        this.searchBox = new EditBox(Minecraft.getInstance().font, this.getX(), this.getY(), this.width, this.height, CommonComponents.EMPTY);
        this.searchBox.setHint(Component.literal(value));
        this.searchBox.setResponder(this::filterEntries);

        for (String entry : entries)
            this.entries.add(new Entry(width, height, entry));

        this.filteredEntries.addAll(this.entries);
    }

    public boolean isShowingEntries() {
        return this.showEntries;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        this.repositionEntries();
        this.searchBox.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

        if (this.showEntries && !this.filteredEntries.isEmpty()) {
            int listY = this.getY() + this.height + 1;
            int actualHeight = getCurrentHeight();
            if (actualHeight <= 0) return;

            graphics.fill(this.getX(), listY, this.getX() + this.width, listY + actualHeight, 0xFF000000);
            graphics.enableScissor(this.getX(), listY, this.getX() + this.width, listY + actualHeight);

            for (Entry entry : this.filteredEntries) {
                entry.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
                if (entry.isHovered()) {
                    graphics.fill(entry.getX(), entry.getY(), entry.getX() + entry.getWidth(), entry.getY() + entry.getHeight(), 0x33FFFFFF);
                }
            }
            graphics.disableScissor();

            int contentHeight = this.filteredEntries.size() * this.height;
            if (contentHeight > actualHeight) {
                int scrollbarWidth = 3;
                int scrollbarX = this.getX() + this.width - scrollbarWidth;
                graphics.fill(scrollbarX, listY, scrollbarX + scrollbarWidth, listY + actualHeight, 0xFF555555);

                int thumbHeight = Math.max(10, (int) ((float) actualHeight * actualHeight / contentHeight));
                int maxScroll = contentHeight - actualHeight;
                int thumbOffset = (int) ((float) this.scrollOffset / maxScroll * (actualHeight - thumbHeight));

                graphics.fill(scrollbarX, listY + thumbOffset, scrollbarX + scrollbarWidth, listY + thumbOffset + thumbHeight, 0xFFAAAAAA);
            }
        }
    }

    public int getCurrentHeight() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int listTop = this.getY() + this.height + 2;
        int spaceToBottom = screenHeight - listTop - 40;
        int preferredHeight = (int) (screenHeight * 0.6);
        int totalContentHeight = this.filteredEntries.size() * this.height;

        return Math.max(0, Math.min(totalContentHeight, Math.min(preferredHeight, spaceToBottom)));
    }

    public void updateValue(String value) {
        this.setFocused(true);
        this.value = value;
        this.searchBox.setValue(value);
        this.searchBox.setHint(Component.literal(value));
        this.scrollOffset = 0;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (this.searchBox.isMouseOver(event.x(), event.y()) && this.searchBox.mouseClicked(event, doubled)) {
            this.searchBox.setFocused(true);
            this.showEntries = true;
            this.filterEntries(this.searchBox.getValue());
            return true;
        }

        if (this.showEntries) {
            for (Entry entry : this.filteredEntries) {
                if (entry.isMouseOver(event.x(), event.y())) {
                    if (entry.mouseClicked(event, doubled)) {
                        this.showEntries = false;
                        return true;
                    }
                }
            }

            if (this.isMouseOver(event.x(), event.y()))
                return true;
        }

        this.showEntries = false;
        return super.mouseClicked(event, doubled);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.showEntries) {
            for (Entry entry : this.filteredEntries)
                entry.mouseMoved(mouseX, mouseY);
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (this.searchBox.isMouseOver(mouseX, mouseY))
            return true;

        if (this.showEntries)
            return mouseX > this.getX() && mouseX < this.getX() + this.width &&
                    mouseY > this.getY() && mouseY < this.getY() + this.getCurrentHeight() + this.getHeight();

        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.showEntries) {
            int contentHeight = this.filteredEntries.size() * this.height;
            int visibleHeight = getCurrentHeight();
            int maxScroll = Math.max(0, contentHeight - visibleHeight);

            this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int) (scrollY * 12)));
            this.repositionEntries();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return this.searchBox.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        return this.searchBox.isFocused() && this.searchBox.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.searchBox.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.searchBox.keyReleased(event) || super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.searchBox.charTyped(event) || super.charTyped(event);
    }

    public void filterEntries(String text) {
        String filter = text.toLowerCase().trim();
        this.filteredEntries.clear();

        boolean matchesFully = false;
        for (Entry entry : this.entries) {
            if (entry.text.equalsIgnoreCase(filter)) {
                matchesFully = true;
                break;
            }
        }

        for (Entry entry : this.entries) {
            if (filter.isEmpty() || matchesFully || entry.getText().toLowerCase().contains(filter))
                this.filteredEntries.add(entry);
        }

        this.scrollOffset = 0;
        this.repositionEntries();
    }

    private void repositionEntries() {
        int y = this.getY() + this.height + 1 - this.scrollOffset;
        for (int i = 0; i < this.filteredEntries.size(); i++) {
            Entry entry = this.filteredEntries.get(i);
            entry.setX(this.getX());
            entry.setY(y + (i * entry.getHeight()));
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);

       this.searchBox.setY(y);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.searchBox.setFocused(focused);

        Window window = Minecraft.getInstance().getWindow();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        if (!focused && !this.isMouseOver(mouseHandler.getScaledXPos(window), mouseHandler.getScaledYPos(window)))  {
            this.showEntries = false;

        }
    }

    @Environment(EnvType.CLIENT)
    public class Entry extends AbstractButton {

        private final String text;

        public Entry(int width, int height, String text) {
            super(0, 0, width, height, CommonComponents.EMPTY);

            this.text = text;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            ComboBoxWidget.this.updateValue(text);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            Font font = Minecraft.getInstance().font;

            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), CommonColors.BLACK);
            graphics.text(font, this.text, this.getX() + 4, this.getY() + (font.lineHeight / 2) + 2, CommonColors.WHITE);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            int listTop = ComboBoxWidget.this.getY() + ComboBoxWidget.this.height;
            int listBottom = listTop + 400;
            return super.isMouseOver(mouseX, mouseY) && mouseY >= listTop && mouseY <= listBottom;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {

        }

        public String getText() {
            return this.text;
        }

    }

}
