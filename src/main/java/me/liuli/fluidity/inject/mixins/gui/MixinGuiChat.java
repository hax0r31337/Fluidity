package me.liuli.fluidity.inject.mixins.gui;

import me.liuli.fluidity.Fluidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Arrays;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {
    private final Minecraft mc = Minecraft.getMinecraft();

    @Shadow
    protected GuiTextField inputField;

    @Shadow
    private boolean waitingOnAutocomplete;

    @Shadow
    public abstract void onAutocompleteResponse(String[] p_onAutocompleteResponse_1_);

    /**
     * only trust message in KeyTyped to anti some client click check (like old zqat.top)
      */
    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void keyTyped(char typedChar, int keyCode, CallbackInfo callbackInfo) {
        String text=inputField.getText();
        if(text.startsWith(Fluidity.commandManager.getPrefix())) {
            if (keyCode == 28 || keyCode == 156) {
                Fluidity.commandManager.handleCommand(text);
                callbackInfo.cancel();
                mc.ingameGUI.getChatGUI().addToSentMessages(text);
                Minecraft.getMinecraft().displayGuiScreen(null);
            }else{
                Fluidity.commandManager.autoComplete(text);
            }
        }
    }

    /**
     * bypass click command auth like kjy.pub
      */
    @Inject(method = "setText", at = @At("HEAD"), cancellable = true)
    private void setText(String newChatText, boolean shouldOverwrite, CallbackInfo callbackInfo) {
        if(shouldOverwrite&&newChatText.startsWith(Fluidity.commandManager.getPrefix())){
            this.inputField.setText(Fluidity.commandManager.getPrefix()+"say "+newChatText);
            callbackInfo.cancel();
        }
    }

    /**
     * Adds client command auto completion and cancels sending an auto completion request packet
     * to the server if the message contains a client command.
     *
     * @author NurMarvin
     */
    @Inject(method = "sendAutocompleteRequest", at = @At("HEAD"), cancellable = true)
    private void handleClientCommandCompletion(String full, final String ignored, CallbackInfo callbackInfo) {
        if (Fluidity.commandManager.autoComplete(full)) {
            waitingOnAutocomplete = true;

            String[] latestAutoComplete = Fluidity.commandManager.getLatestAutoComplete();

            if (full.toLowerCase().endsWith(latestAutoComplete[latestAutoComplete.length - 1].toLowerCase()))
                return;

            this.onAutocompleteResponse(latestAutoComplete);

            callbackInfo.cancel();
        }
    }

    @Inject(method = "onAutocompleteResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;autocompletePlayerNames(F)V", shift = At.Shift.BEFORE), cancellable = true)
    private void onAutocompleteResponse(String[] autoCompleteResponse, CallbackInfo callbackInfo) {
        if (Fluidity.commandManager.getLatestAutoComplete().length != 0) callbackInfo.cancel();
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        if (Fluidity.commandManager.getLatestAutoComplete().length > 0 && !inputField.getText().isEmpty() && inputField.getText().startsWith(Fluidity.commandManager.getPrefix())) {
            String[] latestAutoComplete = Fluidity.commandManager.getLatestAutoComplete();
            String[] textArray = inputField.getText().split(" ");
            String text = textArray[textArray.length - 1];
            Object[] result = Arrays.stream(latestAutoComplete).filter((str) -> str.toLowerCase().startsWith(text.toLowerCase())).toArray();
            String resultText = "";
            if(result.length > 0)
                resultText = ((String)result[0]).substring(Math.min(((String)result[0]).length(),text.length()));

            mc.fontRendererObj.drawStringWithShadow(resultText, inputField.xPosition + mc.fontRendererObj.getStringWidth(inputField.getText()), inputField.yPosition, new Color(165, 165, 165).getRGB());
        }
    }
}