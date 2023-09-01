package net.autowines.overlay;

import net.autowines.AutoWines;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.autowines.overlay.AutoWinesOverlayHelper.*;


public class AutoWinesOverlay extends OverlayPanel {
    @Inject
    private Client client;

    @Inject
    AutoWinesOverlay(AutoWines autoWines) {
        super(autoWines);
        setPosition(OverlayPosition.BOTTOM_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            long timeElapsed = System.currentTimeMillis() - timeBegan;

            panelComponent.setPreferredSize(new Dimension(275, 800));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Auto Wines")
                    .color(Color.magenta)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Ran: " + ft(timeElapsed))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Wines made: " + wineAmount)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current State: " + currentState)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .build());


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return super.render(graphics);
    }
}