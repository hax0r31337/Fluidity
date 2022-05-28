package me.liuli.fluidity.inject.mixins.client;

import me.liuli.fluidity.Fluidity;
import me.liuli.fluidity.event.ClickBlockEvent;
import me.liuli.fluidity.event.KeyEvent;
import me.liuli.fluidity.event.ScreenEvent;
import me.liuli.fluidity.event.WorldEvent;
import me.liuli.fluidity.util.client.ClientUtilsKt;
import net.minecraft.block.material.Material;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    private int leftClickCounter;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public EffectRenderer effectRenderer;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public abstract void displayGuiScreen(GuiScreen guiScreenIn);

    @Shadow private boolean fullscreen;

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void createDisplay(CallbackInfo callbackInfo) {
        ClientUtilsKt.setTitle("Loading Minecraft...");
    }

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        Fluidity.INSTANCE.init();
    }

    @Inject(method = "startGame", at = @At("RETURN"))
    private void startGame(CallbackInfo callbackInfo) {
        Fluidity.INSTANCE.load();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKey(CallbackInfo callbackInfo) {
        if(Keyboard.getEventKeyState() && currentScreen == null)
            Fluidity.eventManager.callEvent(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
    }

    @Inject(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovingObjectPosition;getBlockPos()Lnet/minecraft/util/BlockPos;"))
    private void onClickBlock(CallbackInfo callbackInfo) {
        if (this.leftClickCounter == 0 && theWorld.getBlockState(objectMouseOver.getBlockPos()).getBlock().getMaterial() != Material.air) {
            Fluidity.eventManager.callEvent(new ClickBlockEvent(objectMouseOver.getBlockPos(), this.objectMouseOver.sideHit));
        }
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(WorldClient p_loadWorld_1_, String p_loadWorld_2_, final CallbackInfo callbackInfo) {
        Fluidity.eventManager.callEvent(new WorldEvent(p_loadWorld_1_));
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
        Fluidity.eventManager.callEvent(new ScreenEvent(currentScreen));
    }

    @Overwrite
    private void sendClickBlockToController(boolean leftClick) {
        if(!leftClick)
            this.leftClickCounter = 0;

        if (this.leftClickCounter <= 0 && !this.thePlayer.isUsingItem()) {
            if(leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockPos = this.objectMouseOver.getBlockPos();

                if(this.leftClickCounter == 0)
                    Fluidity.eventManager.callEvent(new ClickBlockEvent(blockPos, this.objectMouseOver.sideHit));

                if(this.theWorld.getBlockState(blockPos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockPos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockPos, this.objectMouseOver.sideHit);
                    this.thePlayer.swingItem();
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo callbackInfo) {
        try{
            Fluidity.INSTANCE.shutdown();
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/LoadingScreenRenderer;resetProgressAndMessage(Ljava/lang/String;)V"))
    public void loadWorld(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/LoadingScreenRenderer;displayLoadingString(Ljava/lang/String;)V"))
    public void loadWorld1(LoadingScreenRenderer loadingScreenRenderer, String string) {
    }

    @Redirect(method="loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at=@At(value="INVOKE", target="Ljava/lang/System;gc()V", remap=false))
    public void loadWorld2() {
    }

    @Inject(method="toggleFullscreen()V", at=@At(value="INVOKE", target="Lorg/lwjgl/opengl/Display;setFullscreen(Z)V", shift=At.Shift.AFTER, remap=false), require=1, allow=1)
    private void toggleFullscreen(CallbackInfo callbackInfo) {
        if (!this.fullscreen) {
            Display.setResizable(false);
            Display.setResizable(true);
        }
    }
}
