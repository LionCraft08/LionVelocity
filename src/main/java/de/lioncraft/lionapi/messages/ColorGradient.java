package de.lioncraft.lionapi.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public final class ColorGradient {
    private ColorGradient(){}
    public static Component getNewGradiant(@NotNull String text, TextColor color1, TextColor color2){
        int amountOfCharakters = text.length() - 1;
        if(amountOfCharakters == 0){
            return Component.text(text, color1);
        }
        double redStep = ((double) color1.red() - (double) color2.red()) / (double) amountOfCharakters;
        double greenStep = ((double) color1.green()- (double) color2.green()) / (double) amountOfCharakters;
        double blueStep = ((double) color1.blue()- (double) color2.blue()) / (double) amountOfCharakters;
        int red = color1.red();
        int green = color1.green();
        int blue = color1.blue();
        Component c = Component.text("");
        for(char s : text.toCharArray()){
            c = c.append(Component.text(s, TextColor.color(red, green, blue)));
            red -= (int) Math.round(redStep);
            green -= (int) Math.round(greenStep);
            blue -= (int) Math.round(blueStep);
            red = correct(red);
            green = correct(green);
            blue = correct(blue);
        }
        return c;
    }
    private static int correct(int value){
        if(value < 0){
            value = 0;
        } else if (value > 255) {
            value = 255;
        }
        return value;
    }
}
