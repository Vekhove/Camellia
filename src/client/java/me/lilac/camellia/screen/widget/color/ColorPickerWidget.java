package me.lilac.camellia.screen.widget.color;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

@Environment(EnvType.CLIENT)
public class ColorPickerWidget extends AbstractWidget {

    private final Consumer<Integer> onSelected;
    private final EditBox hexEditBox;
    private final List<CommonColorButton> colorButtons;
    private EditBox transparencyEditBox;
    private float[] hsb;
    private int color;
    private DragArea draggingArea;
    private boolean isUpdating;

    public ColorPickerWidget(int x, int y, int width, int height, int selectedColor, boolean hasAlpha, Consumer<Integer> onSelected) {
        super(x, y, width, height, CommonComponents.EMPTY);

        this.onSelected = onSelected;
        this.color = selectedColor;
        this.hsb = Color.RGBtoHSB((this.color >> 16) & 0xFF, (this.color >> 8) & 0xFF, this.color & 0xFF, null);

        this.hexEditBox = new EditBox(Minecraft.getInstance().font, this.getX() + 10, this.getY() + 200, 50, 18, CommonComponents.EMPTY);
        this.hexEditBox.setMaxLength(7);
        this.hexEditBox.setResponder(value -> {
            if (!value.matches("^#([0-9a-fA-F]{1,6})?$"))
                return;

            if (this.isUpdating)
                return;

            String hex = value.replace("#", "");
            try {
                int rgb = Integer.parseInt(hex, 16) & 0x00FFFFFF;
                int alpha = this.color & 0xFF000000;
                this.color = alpha | rgb;
                this.hsb = Color.RGBtoHSB((this.color >> 16) & 0xFF, (this.color >> 8) & 0xFF, this.color & 0xFF, null);
                this.onSelected.accept(this.color);
            } catch (NumberFormatException ignored) {

            }
        });
        this.updateHexText();

        if (hasAlpha) {
            this.transparencyEditBox = new EditBox(Minecraft.getInstance().font, this.getX() + 140, this.getY() + 200, 32, 18, CommonComponents.EMPTY);
            this.transparencyEditBox.setMaxLength(4);
            this.transparencyEditBox.setResponder(value -> {
                if (!value.matches("^(\\d+\\.?\\d*)?%?$"))
                    return;

                String stripped = value.replace("%", "");
                if (stripped.isEmpty())
                    return;

                try {
                    float percent = Float.parseFloat(stripped) / 100.0F;
                    int alpha = (int) (Math.max(0.0F, Math.min(1.0F, percent)) * 255.0F);
                    this.color = (alpha << 24) | (this.color & 0x00FFFFFF);
                    this.onSelected.accept(this.color);
                } catch (NumberFormatException ignored) {

                }
            });
            this.updateAlphaText();
        }

        this.colorButtons = new ArrayList<>();

        int buttonX = 0;
        int buttonY = 0;
        int index = 0;
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (!formatting.isColor())
                continue;

            this.colorButtons.add(new CommonColorButton(this.getX() + 14 + buttonX, this.getY() + 148 + buttonY, 16, 16, this.removeAlpha(formatting.getColor())));
            index++;
            if (index == 8) {
                buttonX = 0;
                buttonY = 20;
            } else {
                buttonX += 20;
            }
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.nextStratum();

        // Box and Border
        graphics.fill(this.getX() + 2, this.getY() + 2, this.getX() + this.width - 2, this.getY() + this.height - 2, 0xFF2F2F2F);
        graphics.outline(this.getX(), this.getY(), this.width, this.height, CommonColors.DARK_GRAY);

        // Color Settings
        this.extractColorArea(graphics);
        this.extractHueBar(graphics);

        this.hexEditBox.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
        if (this.transparencyEditBox != null) {
            this.extractAlphaBar(graphics);
            this.transparencyEditBox.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
        }

        for (CommonColorButton button : this.colorButtons)
            button.extractContents(graphics, mouseX, mouseY, deltaTicks);
    }

    private void extractColorArea(GuiGraphicsExtractor graphics) {
        int x = this.getX() + 10;
        int y = this.getY() + 10;
        int x1 = this.getX() + this.width - 10;
        int y1 = this.getY() + 100;

        int hue = Color.HSBtoRGB(this.hsb[0], 1.0f, 1.0f);
        this.submitColorArea(graphics, x, y, x1, y1, hue);

        float width = x1 - x;
        float height = y1 - y;
        float saturation = this.hsb[1];
        float brightness = this.hsb[2];
        int handleX = (int) (x + (saturation * width));
        int handleY = (int) (y + ((1.0F - brightness) * height));

        graphics.fill(handleX - 3, handleY - 3, handleX + 3, handleY + 3, 0xFF000000);
        graphics.fill(handleX - 2, handleY - 2, handleX + 2, handleY + 2, 0xFFFFFFFF);
        graphics.fill(handleX - 1, handleY - 1, handleX + 1, handleY + 1, this.color | 0xFF000000);
    }

    private void extractHueBar(GuiGraphicsExtractor graphics) {
        int x = this.getX() + 10;
        int y = this.getY() + 104;
        int x1 = this.getX() + this.width - 10;
        int y1 = this.getY() + 110;

        this.submitHueBar(graphics, x, y, x1, y1);

        float width = x1 - x + 1;
        float hue = this.hsb[0];
        int handleX = (int) (x + (hue * width));
        int top = y - 1;
        int bottom = y1 + 1;

        graphics.fill(handleX - 1, top, handleX + 1, bottom, 0xFFFFFFFF);
        graphics.fill(handleX - 2, top, handleX - 1, bottom, 0xFF000000);
        graphics.fill(handleX + 1, top, handleX + 2, bottom, 0xFF000000);
    }

    private void extractAlphaBar(GuiGraphicsExtractor graphics) {
        int x = this.getX() + 10;
        int y = this.getY() + 114;
        int x1 = this.getX() + this.width - 10;
        int y1 = this.getY() + 120;

        this.submitAlphaBar(graphics, x, y, x1, y1, this.removeAlpha(this.color));

        float width = x1 - x;
        int alpha = (this.color >> 24) & 0xFF;
        float alphaPercent = alpha / 255.0F;
        int handleX = (int) (x + (alphaPercent * width));
        int top = y - 1;
        int bottom = y1 + 1;

        graphics.fill(handleX - 2, top, handleX + 2, bottom, 0xFF000000);
        graphics.fill(handleX - 1, top, handleX + 1, bottom, 0xFFFFFFFF);
    }

    private float getAlphaPercentage() {
        int alpha = (this.color >> 24) & 0xFF;
        return alpha / 255.0F;
    }

    private void updateHexText() {
        this.hexEditBox.setValue(String.format("#%06X", (this.color & 0xFFFFFF)));
    }

    private void updateAlphaText() {
        if (this.transparencyEditBox != null)
            this.transparencyEditBox.setValue((Mth.ceil(this.getAlphaPercentage() * 100.0F)) + "%");
    }

    private int removeAlpha(int color) {
        return (255 << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        this.draggingArea = DragArea.NONE;
        this.hexEditBox.setFocused(false);
        if (this.transparencyEditBox != null)
            this.transparencyEditBox.setFocused(false);

        for (CommonColorButton button : this.colorButtons) {
            if (button.mouseClicked(event, doubled)) {
                this.updateHexText();
                this.updateAlphaText();
                return true;
            }
        }

        if (this.hexEditBox.mouseClicked(event, doubled)) {
            this.hexEditBox.setFocused(true);
            return true;
        }

        if (this.transparencyEditBox != null && this.transparencyEditBox.mouseClicked(event, doubled)) {
            this.transparencyEditBox.setFocused(true);
            return true;
        }

        if (event.x() >= this.getX() + 10 && event.x() <= this.getX() + this.width - 10
                && event.y() >= this.getY() + 10 && event.y() <= this.getY() + 100) {
            this.updateColor(event.x(), event.y());
            this.draggingArea = DragArea.COLOR_AREA;
            return true;
        }

        else if (event.x() >= this.getX() + 10 && event.x() <= this.getX() + this.width - 10
                && event.y() >= this.getY() + 104 && event.y() <= this.getY() + 110) {
            this.updateHue(event.x());
            this.draggingArea = DragArea.HUE_BAR;
            return true;
        }

        else if (event.x() >= this.getX() + 10 && event.x() <= this.getX() + this.width - 10
                && event.y() >= this.getY() + 114 && event.y() <= this.getY() + 120) {
            this.updateAlpha(event.x());
            this.draggingArea = DragArea.ALPHA_BAR;
            return true;
        }

        else return event.x() >= this.getX() && event.x() <= this.getX() + this.width
                    && event.y() >= this.getY() && event.y() <= this.getY() + this.height;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.draggingArea = DragArea.NONE;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (this.draggingArea == DragArea.COLOR_AREA) {
            this.updateColor(event.x(), event.y());
            return true;
        }

        if (this.draggingArea == DragArea.HUE_BAR) {
            this.updateHue(event.x());
            return true;
        }

        if (this.draggingArea == DragArea.ALPHA_BAR) {
            this.updateAlpha(event.x());
            return true;
        }

        return this.hexEditBox.mouseDragged(event, mouseX, mouseY) ||
                (this.transparencyEditBox != null && this.transparencyEditBox.mouseDragged(event, mouseX, mouseY)) ||
                super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.hexEditBox.keyPressed(event) || (this.transparencyEditBox != null && this.transparencyEditBox.keyPressed(event)))
            return true;

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.hexEditBox.charTyped(event) || (this.transparencyEditBox != null && this.transparencyEditBox.charTyped(event)))
            return true;

        return super.charTyped(event);
    }

    private void updateColor(double mouseX, double mouseY) {
        this.isUpdating = true;

        this.hsb[1] = (float) (Math.max(this.getX() + 10, Math.min(this.getX() + this.width - 10, mouseX)) - (this.getX() + 10)) / (this.width - 20);
        this.hsb[2] = 1.0f - ((float) (Math.max(this.getY() + 10, Math.min(this.getY() + 100, mouseY)) - (this.getY() + 10)) / 90.0f);
        this.color = (((this.color >> 24) & 0xFF) << 24) | (Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]) & 0x00FFFFFF);

        this.updateHexText();
        this.onSelected.accept(this.color);
        this.isUpdating = false;
    }

    private void updateHue(double mouseX) {
        this.isUpdating = true;

        float min = this.getX() + 10;
        float max = this.getX() + this.width - 10;
        float range = max - min;

        float val = (float) ((mouseX - min) / range);

        // Stupid little slider breaks if we let it get to 1.0
        this.hsb[0] = Math.max(0.0F, Math.min(0.999F, val));

        int rgb = Color.HSBtoRGB(this.hsb[0], this.hsb[1], this.hsb[2]) & 0x00FFFFFF;
        this.color = (this.color & 0xFF000000) | rgb;

        this.updateHexText();
        this.onSelected.accept(this.color);
        this.isUpdating = false;
    }

    private void updateAlpha(double mouseX) {
        this.isUpdating = true;

        float alpha = (float) (Math.max(this.getX() + 10, Math.min(this.getX() + this.width - 10, mouseX)) - (this.getX() + 10)) / (this.width - 20);
        int a = Math.round(alpha * 255.0f);
        this.color = ((a & 0xFF) << 24) | (this.color & 0x00FFFFFF);

        this.updateHexText();
        this.updateAlphaText();
        this.onSelected.accept(this.color);

        this.isUpdating = false;
    }

    private void submitColorArea(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.guiRenderState.addGuiElement(
                        new ColorAreaRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()),
                                x0, y0, x1, y1, color, graphics.scissorStack.peek())
        );
    }

    private void submitHueBar(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1) {
        graphics.guiRenderState.addGuiElement(
                new HueBarRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()),
                        x0, y0, x1, y1, graphics.scissorStack.peek())
        );
    }

    private void submitAlphaBar(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.guiRenderState.addGuiElement(
                new AlphaBarRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()),
                        x0, y0, x1, y1, color, graphics.scissorStack.peek())
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    @Override
    public void setX(int x) {
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        this.hexEditBox.setY(y + 124);
        if (this.transparencyEditBox != null)
            this.transparencyEditBox.setY(y + 124);

        int index = 0;
        for (CommonColorButton button : this.colorButtons) {
            int buttonY = index < 8 ? 148 : 168;
            button.setY(y + buttonY);

            index++;
        }
    }

    @Environment(EnvType.CLIENT)
    public enum DragArea {

        NONE,
        COLOR_AREA,
        HUE_BAR,
        ALPHA_BAR

    }

    @Environment(EnvType.CLIENT)
    public class CommonColorButton extends AbstractButton {

        private final int color;

        public CommonColorButton(int x, int y, int width, int height, int color) {
            super(x, y, width, height, CommonComponents.EMPTY);

            this.color = color;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            graphics.outline(this.getX() - 1, this.getY(), this.width, this.height, CommonColors.WHITE);
            graphics.fill(this.getX() + 1, this.getY() + 2, this.getX() + this.width - 3, this.getY() + this.height - 2, (255 << 24) | (this.color & 0x00FFFFFF));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            ColorPickerWidget.this.color = this.color;
            ColorPickerWidget.this.hsb = Color.RGBtoHSB((this.color >> 16) & 0xFF, (this.color >> 8) & 0xFF, this.color & 0xFF, null);
            ColorPickerWidget.this.onSelected.accept(this.color);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {

        }

    }

    @Environment(EnvType.CLIENT)
    public record ColorAreaRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                       int x0, int y0, int x1, int y1, int color,
                                       ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {

        public ColorAreaRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                    int x0, int y0, int x1, int y1, int color, ScreenRectangle screenRectangle) {
            this(pipeline, textureSetup, pose, x0, y0, x1, y1, color, screenRectangle, getBounds(x0, y0, x1, y1, pose, screenRectangle));
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y0).setColor(0xFFFFFFFF);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y1).setColor(0xFFFFFFFF);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y1).setColor(this.color);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y0).setColor(this.color);

            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y0).setColor(0x00000000);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y1).setColor(0xFF000000);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y1).setColor(0xFF000000);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y0).setColor(0x00000000);
        }

        private static ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = new ScreenRectangle(i, j, k - i, l - j).transformMaxBounds(matrix3x2fc);
            return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
        }

    }

    @Environment(EnvType.CLIENT)
    public record HueBarRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                    int x0, int y0, int x1, int y1,
                                    ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {

        public HueBarRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                 int x0, int y0, int x1, int y1, ScreenRectangle screenRectangle) {
            this(pipeline, textureSetup, pose, x0, y0, x1, y1, screenRectangle, getBounds(x0, y0, x1, y1, pose, screenRectangle));
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            int[] rainbow = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
            float segmentWidth = (float) (this.x1 - this.x0) / (rainbow.length - 1);

            for (int i = 0; i < rainbow.length - 1; i++) {
                float xStart = this.x0 + (i * segmentWidth);
                float xEnd = xStart + segmentWidth;
                int colorStart = rainbow[i];
                int colorEnd = rainbow[i + 1];

                vertexConsumer.addVertexWith2DPose(this.pose(), xStart, this.y0).setColor(colorStart);
                vertexConsumer.addVertexWith2DPose(this.pose(), xStart, this.y1).setColor(colorStart);
                vertexConsumer.addVertexWith2DPose(this.pose(), xEnd, this.y1).setColor(colorEnd);
                vertexConsumer.addVertexWith2DPose(this.pose(), xEnd, this.y0).setColor(colorEnd);
            }
        }

        private static ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = new ScreenRectangle(i, j, k - i, l - j).transformMaxBounds(matrix3x2fc);
            return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
        }

    }

    @Environment(EnvType.CLIENT)
    public record AlphaBarRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                      int x0, int y0, int x1, int y1, int color,
                                      ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {

        public AlphaBarRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                   int x0, int y0, int x1, int y1, int color, ScreenRectangle screenRectangle) {
            this(pipeline, textureSetup, pose, x0, y0, x1, y1, color, screenRectangle, getBounds(x0, y0, x1, y1, pose, screenRectangle));
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            int size = 3;
            for (int i = 0; i < (this.x1 - this.x0); i += size) {
                for (int j = 0; j < (this.y1 - this.y0); j += size) {
                    int checkColor = ((i / size + j / size) % 2 == 0) ? 0xFFFFFFFF : 0xFFD0D0D0;

                    float xStart = this.x0 + i;
                    float yStart = this.y0 + j;
                    float xEnd = Math.min(xStart + size, this.x1);
                    float yEnd = Math.min(yStart + size, this.y1);

                    vertexConsumer.addVertexWith2DPose(this.pose(), xStart, yStart).setColor(checkColor);
                    vertexConsumer.addVertexWith2DPose(this.pose(), xStart, yEnd).setColor(checkColor);
                    vertexConsumer.addVertexWith2DPose(this.pose(), xEnd, yEnd).setColor(checkColor);
                    vertexConsumer.addVertexWith2DPose(this.pose(), xEnd, yStart).setColor(checkColor);
                }
            }

            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y0).setColor(0x00FFFFFF);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x0, this.y1).setColor(0x00FFFFFF);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y1).setColor(this.color);
            vertexConsumer.addVertexWith2DPose(this.pose(), this.x1, this.y0).setColor(this.color);
        }

        private static ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = new ScreenRectangle(i, j, k - i, l - j).transformMaxBounds(matrix3x2fc);
            return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
        }

    }

}
