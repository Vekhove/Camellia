package me.lilac.camellia.screen.widget.list;

import java.util.ArrayList;
import java.util.List;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.input.InputType;
import me.lilac.camellia.input.ListInput;
import me.lilac.camellia.screen.widget.InputWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

@Environment(EnvType.CLIENT)
public abstract class ListInputWidget<T extends InputType<?, ?> & ListInput> extends InputWidget<T> {

    protected final List<Entry> elements;
    private Button addButton;
    protected boolean expanded;

    public ListInputWidget(T parameters, Setting<Object> setting) {
        super(parameters, setting);

        this.expanded = true;
        this.elements = new ArrayList<>();
    }

    public abstract AbstractWidget createElement(Object value);

    @Override
    public void init() {
        this.children.clear();
        this.elements.clear();

        this.addButton = new Button.Builder(Component.literal("+").withStyle(ChatFormatting.GREEN), (button) -> {
            if (this.elements.size() >= this.parameters.getMaxEntries())
                return;

            this.addElement(null);
        }).pos(this.getX() - 26,  2).size(18, 18).build();
        this.addChild(this.addButton);

        List<Object> elements = (List<Object>) this.getCurrentValue();
        for (Object element : elements)
            this.addElement(this.serialize(element));
    }

    private void addElement(Object element) {
        AbstractWidget widget = this.createElement(element);
        int index = this.elements.size() + 1;
        PositionedChild child = new PositionedChild(widget, widget.getX(), widget.getY());
        this.setY(this.getY());

        Button deleteButton = new Button.Builder(Component.literal("-").withStyle(ChatFormatting.RED), (button) -> {
            Entry toRemove = null;
            for (Entry entry : this.elements) {
                if (entry.deleteButton == button) {
                    toRemove = entry;
                    break;
                }
            }

            this.elements.remove(toRemove);
        }).pos(this.getX() - 26, 2 + (20 * (index - 1))).size(18, 18).build();

        this.elements.add(new Entry(child, deleteButton));
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
        graphics.text(Minecraft.getInstance().font, "(" + this.elements.size() + ")", this.getX() - 52, this.getY() + 6, CommonColors.LIGHT_GRAY);
        String icon = this.expanded ? "▼ " : "▶ ";
        graphics.text(Minecraft.getInstance().font, icon, this.getX() - 6, this.getY() + 6, CommonColors.WHITE);

        if (this.expanded) {
            for (int i = this.elements.size() - 1; i >= 0; i--) {
                Entry element = this.elements.get(i);
                element.child.widget().extractRenderState(graphics, mouseX, mouseY, deltaTicks);
                element.deleteButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        for (Entry element : this.elements)
            element.child.widget().setFocused(false);

        if (this.addButton.mouseClicked(event, doubled))
            return true;

        if (event.y() > this.getY() && event.y() < this.getY() + 20) {
            this.expanded = !this.expanded;
            return true;
        } else {
            for (Entry element : this.elements) {
                if (element.child.widget().mouseClicked(event, doubled)) {
                    element.child.widget().setFocused(true);
                    return true;
                }

                if (element.deleteButton.mouseClicked(event, doubled)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubled);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Entry element : this.elements)
            element.child.widget().mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (Entry element : this.elements) {
            if (element.child.widget().mouseReleased(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        for (Entry element : this.elements) {
            if (element.child.widget().isFocused() && element.child.widget().mouseDragged(event, deltaX, deltaY))
                return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Entry element : this.elements) {
            if (element.child.widget().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (Entry element : this.elements) {
            if (element.child.widget().keyPressed(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (Entry element : this.elements) {
            if (element.child.widget().keyReleased(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Entry element : this.elements) {
            if (element.child.widget().charTyped(event))
                return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (Entry element : this.elements) {
            if (element.child.widget().isMouseOver(mouseX, mouseY))
                return true;
        }

        return mouseY > this.getY() && mouseY < this.getY() + this.getCurrentHeight();
    }

    @Override
    public void setFocused(boolean focused) {
        for (Entry element : this.elements)
            element.child.widget().setFocused(focused);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        int index = 0;
        for (Entry element : this.elements) {
            element.child.widget().setY(element.child.y() + y + 24 + (index * 24));
            element.deleteButton.setY(y + 24 + (index * 24));
            index++;
        }
    }

    @Override
    public Object getCurrentValue() {
        return this.setting.getValue();
    }

    @Override
    public Object getDefaultValue() {
        return this.setting.getDefaultValue();
    }

    @Override
    public int getCurrentHeight() {
        return this.expanded ? 24 + (this.elements.size() * 24) : super.getCurrentHeight();
    }

    @Override
    public abstract void save();

    @Override
    public Component getDefaultValueText() {
        return Component.empty();
    }

    @Environment(EnvType.CLIENT)
    public record Entry(PositionedChild child, Button deleteButton)  {

    }

}
