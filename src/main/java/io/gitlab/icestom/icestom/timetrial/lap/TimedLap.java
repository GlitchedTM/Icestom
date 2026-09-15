package io.gitlab.icestom.icestom.timetrial.lap;

import io.gitlab.icestom.icestom.timetrial.Split;
import io.gitlab.icestom.icestom.track.Track;
import io.gitlab.icestom.icestom.util.TextFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimedLap implements TimedLapResultSource {

    private final Track track;
    @Nullable private final TimedLapResultSource bestPreviousResult;

    private final List<Split> splits = new ArrayList<>();
    private final Map<Integer, Pos> ticks = new HashMap<>();

    private long msStart;
    private int lastReachedCheckpoint = -1;

    private long recentSplit = 0;

    public TimedLap(Track track, @Nullable TimedLapResultSource bestPreviousResult, Split split) {

        this.track = track;
        this.bestPreviousResult = bestPreviousResult;

        advanceCheckpoint(split);
    }

    public TimedLap(Track track, @Nullable TimedLapResultSource bestPreviousResult) {
        this.track = track;
        this.bestPreviousResult = bestPreviousResult;
    }

    public boolean advanceCheckpoint(Split split) {
        int this_checkpoint_index = track.wrapCheckpointIndex(lastReachedCheckpoint + 1);

        if (split.checkpoint_no() != this_checkpoint_index)
            throw new RuntimeException("Wrong checkpoint number sent to timed lap");

        if (lastReachedCheckpoint == -1) {
            msStart = split.ms();
        }

        Split local_split = split.offset(msStart);
        splits.add(local_split);

        boolean is_last = track.isLastCheckpoint(this_checkpoint_index) && lastReachedCheckpoint != -1;

        int globalIndex = ++lastReachedCheckpoint;

        if (bestPreviousResult != null) {
            if (bestPreviousResult.splits().size() <= globalIndex) {
                recentSplit = 0;
            } else {
                Split best_previous = bestPreviousResult.splits().get(globalIndex);
                recentSplit = local_split.ms() - best_previous.ms();
            }
        }

        return is_last;
    }

    public long getCurrentTime(long worldAge) {
        if (splits.isEmpty()) return 0;

        return worldAge * 50 - getMsStart();
    }

    private static final TextColor CHECKPOINT_PROGRESS_COLOR = TextColor.color(0xFF5FD7);
    private static final TextColor FINISH_COLOR = TextColor.color(0xFFD54A);
    private static final Component ACTION_BAR_SEPARATOR = Component.text("   ");

    public Component getActionBar(long worldAge, boolean showDeltas) {
        long time = Math.max(0, worldAge * 50 - getMsStart());

        float t_seconds = (float) Math.ceil((float) time / 50) * 50 / 1000;

        Component text = Component.text(String.format("%.2f", t_seconds));

        if (bestPreviousResult != null && showDeltas && lastReachedCheckpoint != 0) {
            text = text
                    .append(ACTION_BAR_SEPARATOR)
                    .append(TextFormatter.getDelta(recentSplit));
        }

        return text
                .append(ACTION_BAR_SEPARATOR)
                .append(checkpointProgress());
    }

    private Component checkpointProgress() {
        int reached = Math.max(0, lastReachedCheckpoint);
        int total = track.getScoredCheckpointCount();

        Component denominator = reached >= total
                ? Component.text("FINISH", FINISH_COLOR)
                : Component.text("C" + total, CHECKPOINT_PROGRESS_COLOR);

        return Component.text("C" + reached + " / ", CHECKPOINT_PROGRESS_COLOR)
                .append(denominator);
    }

    /** Non-looped tracks only: whether the last reached checkpoint is the finish line. */
    public boolean isAtFinishLine() {
        if (track.isLooped()) return false;

        return lastReachedCheckpoint > 0 && track.isLastCheckpoint(lastReachedCheckpoint);
    }

    public long getMsStart() { return msStart; }
    public int getLastReachedCheckpoint() { return lastReachedCheckpoint; }

    public long getRecentSplit() { return recentSplit; }

    @Override
    public List<Split> splits() { return splits; }

    @Override
    public Map<Integer, Pos> ticks() { return ticks; }

    public @Nullable TimedLapResultSource getBestPreviousResult() {
        return bestPreviousResult;
    }
}
