package net.autobarbfisher.overlay;

import net.autobarbfisher.AutoBarbFisher;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.autobarbfisher.overlay.AutoBarbFisherOverlayHelper.*;


public class AutoBarbFisherOverlay extends OverlayPanel {
    @Inject
    private Client client;

    @Inject
    AutoBarbFisherOverlay(AutoBarbFisher autoBarbFisher) {
        super(autoBarbFisher);
        setPosition(OverlayPosition.BOTTOM_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            long timeElapsed = System.currentTimeMillis() - timeBegan;
            xpGained = client.getSkillExperience(Skill.FISHING) - expstarted;

            int xpPerHour = (int) (xpGained / ((System.currentTimeMillis() - timeBegan) / 3600000.0D));
            nextLevelXp = XP_TABLE[client.getRealSkillLevel(Skill.FISHING) + 1];
            xpTillNextLevel = nextLevelXp - client.getSkillExperience(Skill.FISHING);
            if (xpGained >= 1) {
                timeTNL = (long) ((xpTillNextLevel / xpPerHour) * 3600000);
            }
            panelComponent.setPreferredSize(new Dimension(275, 800));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("3T Barb Fisher")
                    .color(Color.magenta)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time Ran: " + ft(timeElapsed))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Fishing Exp Gained (hr): " + (xpGained) + " (" + xpPerHour + ")")
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Fishing Levels Gained: " + (client.getRealSkillLevel(Skill.FISHING) - startinglevel))
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time till next level: " + ft(timeTNL))
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