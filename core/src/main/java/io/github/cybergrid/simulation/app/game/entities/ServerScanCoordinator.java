package io.github.cybergrid.simulation.app.game.entities;

import java.util.List;

import io.github.cybergrid.simulation.engine.managers.EntityManager;

public class ServerScanCoordinator {
    public static final class ScanSnapshot {
        private final int secureScannedCount;
        private final int vulnerableTriggeredCount;
        private final boolean attackEntryDiscovered;

        private ScanSnapshot(int secureScannedCount, int vulnerableTriggeredCount, boolean attackEntryDiscovered) {
            this.secureScannedCount = secureScannedCount;
            this.vulnerableTriggeredCount = vulnerableTriggeredCount;
            this.attackEntryDiscovered = attackEntryDiscovered;
        }

        public int getSecureScannedCount() {
            return secureScannedCount;
        }

        public int getVulnerableTriggeredCount() {
            return vulnerableTriggeredCount;
        }

        public boolean isAttackEntryDiscovered() {
            return attackEntryDiscovered;
        }
    }

    private final EntityManager entityManager;

    public ServerScanCoordinator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ScanSnapshot evaluate(List<Integer> serverIds) {
        int secureScannedCount = 0;
        int vulnerableTriggeredCount = 0;
        boolean attackEntryDiscovered = false;

        for (int serverId : serverIds) {
            ServerEntityBase server = entityManager.getEntityAs(serverId, ServerEntityBase.class);
            if (server == null) {
                continue;
            }

            if (server.countsAsSecureNode() && server.isScanned()) {
                secureScannedCount++;
            }

            if (server.isAttackEntryPoint() && server.isTriggered()) {
                vulnerableTriggeredCount++;
                attackEntryDiscovered = true;
            }
        }

        return new ScanSnapshot(secureScannedCount, vulnerableTriggeredCount, attackEntryDiscovered);
    }
}
