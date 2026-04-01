package me.lilac.camellia.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.lilac.camellia.ClientConfigSync;
import me.lilac.camellia.config.Setting;
import me.lilac.camellia.config.SettingHolder;
import me.lilac.camellia.screen.widget.InputWidget;
import me.lilac.camellia.sync.ServerboundSetSettingsPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public abstract class ConfigScreen extends Screen {

    private final String modId;
    private final Screen parent;
    private final List<ConfigTab> tabs;
    private List<SettingHolder> settingHolders;
    private TabManager tabManager;
    private HeaderAndFooterLayout layout;
    private TabNavigationBar tabNavigationBar;
    private ScreenRectangle tabArea;
    private boolean isOnServer;

    public ConfigScreen(Screen parent, String modId) {
        super(CommonComponents.EMPTY);

        this.modId = modId;
        this.parent = parent;
        this.tabs = new ArrayList<>();
    }

    public int getTextColor() {
        return 0xFFF4DDFF;
    }

    public int getHighlightColor() {
        return 0xFFF4DDFF;
    }

    public abstract SettingHolder getClientSettings();

    public abstract SettingHolder getServerSettings();

    @Override
    protected void init() {
        super.init();

        this.settingHolders = new ArrayList<>();
        this.settingHolders.add(this.getClientSettings());
        ClientLevel level = this.minecraft.level;
        LocalPlayer player = this.minecraft.player;
        if (level != null) {
            if (player == null || Minecraft.getInstance().getCurrentServer() == null) {
                this.settingHolders.add(this.getServerSettings());
            } else if (ClientConfigSync.serverSettingsReceived) {
                this.settingHolders.add(this.getServerSettings());
                this.isOnServer = true;
            }
        } else {
            this.settingHolders.add(this.getServerSettings());
        }

        this.layout = new HeaderAndFooterLayout(this);
        this.tabs.clear();
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

        Map<String, List<Setting<?>>> tabGroups = this.settingHolders.stream()
                .flatMap(holder -> holder.get().stream())
                .filter(s -> s.getTab() != null)
                .collect(Collectors.groupingBy(Setting::getTab));

        TabNavigationBar.Builder builder = TabNavigationBar.builder(this.tabManager, this.width);
        for (String tab : tabGroups.keySet()) {
            ConfigTab configTab = new ConfigTab(tab, tabGroups.get(tab));
            builder.addTabs(configTab);
            this.tabs.add(configTab);
        }

        this.tabNavigationBar = builder.build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());

        this.layout.visitWidgets(child -> {
            child.setTabOrderGroup(1);
            this.addRenderableWidget(child);
        });

        if (!this.tabNavigationBar.getTabs().isEmpty())
            this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar == null)
            return;

        this.tabNavigationBar.updateWidth(this.width);
        this.tabNavigationBar.arrangeElements();

        int bottom = this.tabNavigationBar.getRectangle().bottom();
        this.tabArea = new ScreenRectangle(0, bottom, this.width, this.height - this.layout.getFooterHeight() - bottom);
        this.tabManager.setTabArea(this.tabArea);
        this.layout.setHeaderHeight(bottom);
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public void onDone() {
        for (ConfigTab tab : this.tabs) {
            for (ConfigScreenEntry entry : tab.children) {
                entry.save();
            }
        }

        Player player = Minecraft.getInstance().player;
        if (this.isOnServer && player != null) {
            List<Setting<Object>> settings = new ArrayList<>();
            for (Setting<?> setting : this.getServerSettings().get())
                settings.add((Setting<Object>) setting);

            ClientPlayNetworking.send(new ServerboundSetSettingsPayload(player.getUUID(), settings));
        } else if (player == null || Minecraft.getInstance().getCurrentServer() == null) {
            this.getServerSettings().save();
        }

        this.getClientSettings().save();

        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.tabManager.getCurrentTab() == null)
            return;

        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR,
                0, this.height - this.layout.getFooterHeight() - 2,
                0.0F, 0.0F, this.width, 2, 32, 2);

        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                tab.extractRenderState(graphics, mouseX, mouseY, a);
                break;
            }
        }
    }

    @Override
    public void resize(int i, int j) {
        super.resize(i, j);

        this.init();
        this.repositionElements();
        this.rebuildWidgets();

        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                tab.init();
                break;
            }
        }
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND,
                0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);

        this.extractMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        for (ConfigTab tab : this.tabs) {
            tab.setFocused(false);
        }

        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab && this.tabArea.containsPoint((int) event.x(), (int) event.y())) {
                if (tab.mouseClicked(event, doubled)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubled);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                tab.mouseMoved(mouseX, mouseY);
            }
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.mouseReleased(event))
                    return true;
            }
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.mouseDragged(event, deltaX, deltaY))
                    return true;
            }
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                return tab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.keyPressed(event))
                    return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.keyReleased(event))
                    return true;
            }
        }

        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.charTyped(event))
                    return true;
            }
        }

        return super.charTyped(event);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab) {
                if (tab.isMouseOver(mouseX, mouseY))
                    return true;
            }
        }

        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
        for (ConfigTab tab : this.tabs) {
            if (this.tabManager.getCurrentTab() == tab)
                tab.setFocused(focused);
        }

        super.setFocused(focused);
    }

    @Environment(EnvType.CLIENT)
    public class ConfigTab implements Tab {

        public static final Identifier SCROLLER = Identifier.withDefaultNamespace("widget/scroller");
        public static final Identifier SCROLLER_BACKGROUND = Identifier.withDefaultNamespace("widget/scroller_background");
        private final List<ConfigScreenEntry> children;
        private final Map<String, List<Setting<?>>> sections;
        private final Component title;
        private float scrollAmount;

        public ConfigTab(String id, List<Setting<?>> settings) {
            super();

            this.children = new ArrayList<>();
            this.title = Component.translatable("tab." + ConfigScreen.this.modId + "." + id);
            this.sections = settings.stream()
                    .collect(Collectors.groupingBy(s -> s.getSection() == null ? "none" : s.getSection()));

            this.init();
        }

        public void init() {
            this.children.clear();

            List<Setting<?>> settings = this.sections.get("none");
            if (settings != null) {
                for (Setting<?> setting : settings) {
                    if (setting.isHidden() || setting.getInput() == null)
                        continue;

                    this.children.add(new SettingWidget(setting, ConfigScreen.this.width, 20));
                }
            }

            for (Map.Entry<String, List<Setting<?>>> entry : this.sections.entrySet()) {
                if (entry.getKey().equals("none"))
                    continue;

                this.children.add(new SectionWidget(entry.getKey(), entry.getValue(), ConfigScreen.this.width, 20));
            }
        }

        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            int header = ConfigScreen.this.layout.getHeaderHeight();
            int footer = ConfigScreen.this.layout.getFooterHeight();
            int areaHeight = ConfigScreen.this.height - header - footer;

            int totalContentHeight = 8;
            for (ConfigScreenEntry entry : this.children)
                totalContentHeight += entry.getCurrentHeight() + 4;

            totalContentHeight += (areaHeight / 2);
            int maxScroll = Math.max(0, totalContentHeight - areaHeight);
            int currentScroll = (int) (this.scrollAmount * maxScroll);

            graphics.enableScissor(0, header, ConfigScreen.this.width, header + areaHeight);

            int currentY = header + 4 - currentScroll;
            for (ConfigScreenEntry entry : this.children) {
                entry.setY(currentY);

                if (currentY + entry.getCurrentHeight() > header && currentY < header + areaHeight)
                    entry.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

                currentY += entry.getCurrentHeight() + 4;
            }


            // Do this backwards so entries can display on top of other entries.
            for (int i = this.children.size() - 1; i >= 0; i--) {
                ConfigScreenEntry entry = this.children.get(i);
                int widgetFullHeight = entry.getCurrentHeight();
                int widgetY = entry.getY();

                if (widgetY + widgetFullHeight > header && widgetY < header + areaHeight) {
                    if (entry instanceof SettingWidget settingWidget) {
                        settingWidget.extractChildren(graphics, mouseX, mouseY, deltaTicks);
                    } else if (entry instanceof SectionWidget sectionWidget) {
                        sectionWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
                    }
                }
            }

            graphics.disableScissor();

            if (maxScroll > 0) {
                int trackHeight = areaHeight - 4;
                int scrollbarHeight = Math.max(10, (int) ((float) areaHeight / totalContentHeight * trackHeight));
                int scrollbarY = header + (int) (this.scrollAmount * (trackHeight - scrollbarHeight));

                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND,
                        ConfigScreen.this.width - 10, header, 6, areaHeight - 4);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER,
                        ConfigScreen.this.width - 10, scrollbarY, 6, scrollbarHeight);
            }
        }

        private void updateScroll(int oldMaxScroll) {
            int header = ConfigScreen.this.layout.getHeaderHeight();
            int footer = ConfigScreen.this.layout.getFooterHeight();
            int areaHeight = ConfigScreen.this.height - header - footer;

            int newTotalHeight = 8 + (areaHeight / 2);
            for (ConfigScreenEntry entry : this.children) {
                newTotalHeight += entry.getCurrentHeight() + 4;
            }

            int newMaxScroll = Math.max(0, newTotalHeight - areaHeight);

            if (newMaxScroll > 0) {
                float currentPixelY = this.scrollAmount * oldMaxScroll;
                this.scrollAmount = Mth.clamp(currentPixelY / (float) newMaxScroll, 0.0F, 1.0F);
            } else {
                this.scrollAmount = 0.0F;
            }
        }

        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            int header = ConfigScreen.this.layout.getHeaderHeight();
            int footer = ConfigScreen.this.layout.getFooterHeight();
            int areaHeight = ConfigScreen.this.height - header - footer;

            int totalContentHeight = 8 + (areaHeight / 2);
            for (ConfigScreenEntry entry : this.children)
                totalContentHeight += entry.getCurrentHeight() + 4;

            int maxScroll = Math.max(0, totalContentHeight - areaHeight);
            int currentScroll = (int) (this.scrollAmount * maxScroll);

            int currentY = header + 4 - currentScroll;

            for (ConfigScreenEntry entry : this.children) {
                entry.setY(currentY);

                if (entry.mouseClicked(event, doubled)) {
                    this.updateScroll(maxScroll);
                    return true;
                }

                currentY += entry.getCurrentHeight() + 4;
            }

            return false;
        }

        public void mouseMoved(double mouseX, double mouseY) {
            for (AbstractWidget child : this.children)
                child.mouseMoved(mouseX, mouseY);
        }

        public boolean mouseReleased(MouseButtonEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.mouseReleased(event))
                    return true;
            }

            return false;
        }

        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            for (AbstractWidget child : this.children) {
                if (child.mouseDragged(event, deltaX, deltaY))
                    return true;
            }

            return false;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            for (AbstractWidget section : this.children) {
                if (section.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                    return true;
            }

            int header = ConfigScreen.this.layout.getHeaderHeight();
            int footer = ConfigScreen.this.layout.getFooterHeight();
            int areaHeight = ConfigScreen.this.height - header - footer;

            int totalContentHeight = 8;
            for (ConfigScreenEntry entry : this.children)
                totalContentHeight += entry.getCurrentHeight() + 4;

            totalContentHeight += (areaHeight / 2);
            if (totalContentHeight > areaHeight) {
                int maxScrollHeight = totalContentHeight - areaHeight;

                float offset = (float) (verticalAmount * 20.0F) / maxScrollHeight;
                this.scrollAmount = Mth.clamp(this.scrollAmount - offset, 0.0F, 1.0F);

                return true;
            }

            return false;
        }

        public boolean keyPressed(KeyEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.keyPressed(event))
                    return true;
            }

            return false;
        }

        public boolean keyReleased(KeyEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.keyReleased(event))
                    return true;
            }

            return false;
        }

        public boolean charTyped(CharacterEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.charTyped(event))
                    return true;
            }

            return false;
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            for (AbstractWidget child : this.children) {
                if (child.isMouseOver(mouseX, mouseY))
                    return true;
            }

            return false;
        }

        public void setFocused(boolean focused) {
            for (AbstractWidget child : this.children)
                child.setFocused(focused);
        }

        @Override
        public Component getTabTitle() {
            return this.title;
        }

        @Override
        public Component getTabExtraNarration() {
            return CommonComponents.EMPTY;
        }

        // We want to use tabs, but we don't want to use layouts!

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {

        }

        @Override
        public void doLayout(ScreenRectangle screenRectangle) {

        }

    }

    @Environment(EnvType.CLIENT)
    public class SettingWidget extends ConfigScreenEntry {

        private final Component text;
        private List<FormattedCharSequence> comment;
        private Button resetButton;
        private InputWidget<?> widget;

        public SettingWidget(Setting<?> setting, int width, int height) {
            super(0, 0, width, height);

            this.text = Component.translatable("setting." + ConfigScreen.this.modId + "." + setting.getKey());

            String commentKey = setting.getCommentKey() == null ?
                    "setting." + ConfigScreen.this.modId + "." + setting.getKey() + ".comment" : setting.getCommentKey();
            if (I18n.exists(commentKey))
                this.comment = this.splitComponent(Component.translatable(commentKey));

            if (InputWidgets.has(setting.getInput().getId())) {
                this.widget = InputWidgets.create(setting.getInput().getId(), setting.getInput(), setting);
                if (this.widget != null) {
                    this.widget.setX(this.width - 60);
                    this.widget.setY(this.getY());
                    this.widget.init();

                    this.resetButton = Button.builder(Component.translatable("controls.reset"), button -> {
                        this.widget.reset();
                        this.widget.setY(this.getY());
                    })
                            .tooltip(Tooltip.create(Component.translatable("editGamerule.default", this.widget.getDefaultValueText())))
                            .pos(this.width - 50, this.getY() + 1)
                            .size(34, 18).build();
                }
            }
        }

        private List<FormattedCharSequence> splitComponent(Component component) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            for (String s : component.getString().split("\\n"))
                lines.add(Component.literal(s).getVisualOrderText());

            return lines;
        }

        @Override
        public void save() {
            if (this.widget != null)
                this.widget.save();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            if (this.isHovered() || this.isMouseOver(mouseX, mouseY)) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x44AAAAAA + ConfigScreen.this.getHighlightColor() | (mouseY / 5));

                if (this.comment != null && (this.resetButton != null && !this.resetButton.isHovered()) && (this.widget != null && !this.widget.isMouseOver(mouseX, mouseY)))
                    graphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.comment, mouseX, mouseY);
            }

            if (this.resetButton != null)
                this.resetButton.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

            graphics.text(Minecraft.getInstance().font, this.text,
                    this.getX() + 30, this.getY() + (this.height / 2) - (Minecraft.getInstance().font.lineHeight / 2),
                    ConfigScreen.this.getTextColor());
        }

        public void extractChildren(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            if (this.widget != null)
                this.widget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            return (this.resetButton != null && this.resetButton.mouseClicked(event, doubled)) ||
                    (this.widget != null && this.widget.mouseClicked(event, doubled));
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            if (this.widget != null)
                this.widget.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return this.widget != null && this.widget.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            return this.widget != null && this.widget.mouseDragged(event, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            return this.widget != null && this.widget.isMouseOver(mouseX, mouseY) && this.widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return this.widget != null && this.widget.keyPressed(event);
        }

        @Override
        public boolean keyReleased(KeyEvent event) {
            return this.widget != null && this.widget.keyReleased(event);
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return this.widget != null && this.widget.charTyped(event);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.widget != null && this.widget.isMouseOver(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            if (this.widget != null)
                this.widget.setFocused(focused);

            super.setFocused(focused);
        }

        @Override
        public void setX(int x) {
            super.setX(x);

            if (this.resetButton != null)
                this.resetButton.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);

            if (this.widget != null)
                this.widget.setY(y);

            if (this.resetButton != null)
                this.resetButton.setY(y + 1);
        }

        @Override
        public int getCurrentHeight() {
            return this.widget != null ? this.widget.getCurrentHeight() : this.getHeight();
        }

    }

    @Environment(EnvType.CLIENT)
    public class SectionWidget extends ConfigScreenEntry {

        private final Component text;
        private final List<Setting<?>> settings;
        private final List<SettingWidget> children;
        private boolean expanded;

        public SectionWidget(String text, List<Setting<?>> settings, int width, int height) {
            super(0, 0, width, height);

            this.text = Component.translatable("section." + ConfigScreen.this.modId + "." + text);
            this.settings = settings;
            this.children = new ArrayList<>();
            this.expanded = true;
            this.init();
        }

        public void init() {
            int height = 20;
            for (Setting<?> setting : this.settings) {
                if (setting.isHidden() || setting.getInput() == null)
                    continue;

                this.children.add(new SettingWidget(setting, ConfigScreen.this.width, height));
            }
        }

        @Override
        public void save() {
            for (SettingWidget widget : this.children)
                widget.save();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
            if (this.isHovered())
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
                        0x44AAAAAA + ConfigScreen.this.getHighlightColor() | (mouseY / 5));

            String icon = this.expanded ? "▼ " : "▶ ";
            graphics.text(Minecraft.getInstance().font, Component.literal(icon).append(this.text),
                    this.getX() + 16, this.getY() + (this.height / 2) - (Minecraft.getInstance().font.lineHeight / 2),
                    ConfigScreen.this.getTextColor());

            if (this.expanded) {
                int y = this.getY() + this.height + 2;
                for (SettingWidget child : this.children) {
                    child.setY(y);

                    child.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
                    child.extractChildren(graphics, mouseX, mouseY, deltaTicks);

                    y += child.getCurrentHeight() + 2;
                }
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
            if (event.x() >= this.getX() && event.x() <= this.getX() + this.width &&
                    event.y() >= this.getY() && event.y() <= this.getY() + this.height) {
                this.expanded = !this.expanded;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (this.expanded) {
                int currentY = this.getY() + this.height + 2;
                for (SettingWidget child : this.children) {
                    child.setY(currentY);

                    if (event.x() >= child.getX() && event.x() <= child.getX() + child.getWidth() &&
                            event.y() >= child.getY() && event.y() <= child.getY() + child.getCurrentHeight()) {

                        if (child.mouseClicked(event, doubled))
                            return true;
                    }

                    currentY += child.getCurrentHeight() + 2;
                }
            }

            return false;
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            for (AbstractWidget child : this.children)
                child.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.mouseReleased(event))
                    return true;
            }

            return false;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
            if (this.expanded) {
                int y = this.getY() + this.height + 2;
                for (SettingWidget child : this.children) {
                    child.setY(y);
                    if (child.mouseDragged(event, deltaX, deltaY))
                        return true;

                    y += child.getCurrentHeight() + 2;
                }
            }

            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            for (AbstractWidget child : this.children) {
                if (child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                    return true;
            }

            return false;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.keyPressed(event))
                    return true;
            }

            return false;
        }

        @Override
        public boolean keyReleased(KeyEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.keyReleased(event))
                    return true;
            }

            return false;
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            for (AbstractWidget child : this.children) {
                if (child.charTyped(event))
                    return true;
            }

            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            for (AbstractWidget child : this.children) {
                if (child.isMouseOver(mouseX, mouseY))
                    return true;
            }

            return super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            for (AbstractWidget child : this.children)
                child.setFocused(focused);
        }

        @Override
        public int getCurrentHeight() {
            if (!this.expanded)
                return this.height;

            int size = this.height + 8;
            for (SettingWidget child : this.children)
                size += child.getCurrentHeight();

            return size;
        }

    }

    @Environment(EnvType.CLIENT)
    public abstract static class ConfigScreenEntry extends AbstractWidget {

        public ConfigScreenEntry(int x, int y, int width, int height) {
            super(x, y, width, height, CommonComponents.EMPTY);
        }

        public abstract int getCurrentHeight();

        public abstract void save();

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {

        }

    }

}
