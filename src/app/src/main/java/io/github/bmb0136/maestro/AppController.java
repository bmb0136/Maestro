package io.github.bmb0136.maestro;

import io.github.bmb0136.maestro.core.clip.ChordClip;
import io.github.bmb0136.maestro.core.clip.Clip;
import io.github.bmb0136.maestro.core.clip.PianoRollClip;
import io.github.bmb0136.maestro.core.event.*;
import io.github.bmb0136.maestro.core.clip.ScaleClip;
import io.github.bmb0136.maestro.core.modifier.AddIntervalAboveModifier;
import io.github.bmb0136.maestro.core.modifier.Modifier;
import io.github.bmb0136.maestro.core.modifier.OffsetByIntervalModifier;
import io.github.bmb0136.maestro.core.timeline.Timeline;
import io.github.bmb0136.maestro.core.timeline.TimelineManager;
import io.github.bmb0136.maestro.core.timeline.Track;
import io.github.bmb0136.maestro.core.util.BiHashMap;
import io.github.bmb0136.maestro.core.util.Tuple2;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class AppController implements AutoCloseable {

    private static final BiHashMap<String, Class<?>> MODIFIER_LABELS = new BiHashMap<>();
    private static final HashMap<Class<?>, Supplier<Modifier>> MODIFIER_FACTORIES = new HashMap<>();
    private static final HashMap<Class<?>, ModifierEditorSubscene.Factory> MODIFIER_EDITOR_FACTORIES = new HashMap<>();
    private static final String ADD_MODIFIER = "Add Modifier";

    static {
        MODIFIER_LABELS.add("Offset by Interval", OffsetByIntervalModifier.class);
        MODIFIER_FACTORIES.put(OffsetByIntervalModifier.class, OffsetByIntervalModifier::new);
        MODIFIER_EDITOR_FACTORIES.put(OffsetByIntervalModifier.class, OffsetByIntervalModifierEditor::new);

        MODIFIER_LABELS.add("Add Interval Above", AddIntervalAboveModifier.class);
        MODIFIER_FACTORIES.put(AddIntervalAboveModifier.class, AddIntervalAboveModifier::new);
        MODIFIER_EDITOR_FACTORIES.put(AddIntervalAboveModifier.class, AddIntervalAboveModifierEditor::new);
    }

    @FXML
    private ScrollBar trackScrollBar, timelineScrollBar;
    @FXML
    private Node trackColumn;
    @FXML
    private ScrollPane trackListScrollPane;
    @FXML
    private VBox trackList, modifierList;
    @FXML
    private Region root;
    @FXML
    private Label bpmLabel;
    @FXML
    private TitledPane editorPane;
    @FXML
    private Canvas timelineCanvas;
    @FXML
    private Region timelineParent;
    @FXML
    private ChoiceBox<Object> modifierSelector;
    private final TimelineManager manager = new TimelineManager(1024, new Timeline());
    private final SimpleIntegerProperty bpm = new SimpleIntegerProperty(120);
    private final SimpleDoubleProperty pixelsPerBeat = new SimpleDoubleProperty(60.0);
    private TimelineRenderer timelineRenderer;
    private AutoCloseable changeCallback;
    private final SimpleObjectProperty<Tuple2<UUID, UUID>> selectedClip = new SimpleObjectProperty<>(null);
    private final HashSet<UUID> knownClips = new HashSet<>();
    private final ArrayList<UUID> knownTracks = new ArrayList<>();
    @Nullable
    private UUID lastOpenEditor = null;

    @FXML
    private void initialize() {
        root.getStylesheets().add("/DarkMode.css");

        // Init timeline renderer
        timelineRenderer = new TimelineRenderer(manager, timelineCanvas, pixelsPerBeat, this::timelineCallback);
        timelineRenderer.scrollbarBoundsProperty().bind(timelineScrollBar.boundsInParentProperty());
        timelineCanvas.widthProperty().bind(timelineParent.widthProperty());
        timelineCanvas.heightProperty().bind(root.heightProperty());

        // Sync scroll bars
        trackScrollBar.valueProperty().bindBidirectional(trackListScrollPane.vvalueProperty());
        trackScrollBar.valueProperty().bindBidirectional(timelineRenderer.scrollYProperty());
        timelineScrollBar.valueProperty().bindBidirectional(timelineRenderer.scrollXProperty());
        timelineScrollBar.visibleAmountProperty().bind(timelineRenderer.timelineLengthProperty().map(x -> Math.max(0.1, 1 / x.floatValue())));

        // Auto-resize track scroll bar handle based on track count
        trackList.getChildren().addListener((ListChangeListener<Node>) change -> {
            trackScrollBar.setVisibleAmount(trackListScrollPane.getHeight() / trackList.getHeight());
            trackScrollBar.setVisible(trackList.getHeight() > trackListScrollPane.getHeight());
        });

        // Make column span over scroll bar's space when scroll bar is invisible
        // Can't always make this the case since the scroll bar is semi-transparent
        trackScrollBar.visibleProperty().addListener((observableValue, oldValue, newValue) -> {
            GridPane.setRowSpan(trackColumn, newValue ? 2 : 3);
        });

        trackScrollBar.setVisible(false);
        //timelineScrollBar.setVisible(false);

        bpmLabel.textProperty().bind(bpm.map(value -> "BPM: " + value));

        // Prevent incorrect scrolling
        trackListScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (Math.abs(e.getDeltaX()) > 1e-6) {
                e.consume();
            }
        });

        editorPane.prefWidthProperty().bind(timelineCanvas.widthProperty());
        editorPane.visibleProperty().bind(editorPane.contentProperty().map(Objects::nonNull));

        // Init modifier selector
        modifierSelector.getItems().add(ADD_MODIFIER);
        modifierSelector.setValue(ADD_MODIFIER);
        modifierSelector.getItems().addAll(MODIFIER_LABELS.values1());
        modifierSelector.setOnAction(ignored -> {
            Object value = modifierSelector.getValue();
            if (!(value instanceof String modifierName)) {
                throw new IllegalStateException("Modifier selector value was not a string");
            }
            modifierSelector.setValue(ADD_MODIFIER);
            if (!MODIFIER_LABELS.contains1(modifierName)) {
                return;
            }
            var selectedClip = this.selectedClip.get();
            if (selectedClip == null) {
                new Alert(Alert.AlertType.ERROR, "Please select a clip before adding a modifier", ButtonType.OK).showAndWait();
                return;
            }
            var factory = MODIFIER_FACTORIES.get(MODIFIER_LABELS.get1(modifierName));
            var m = factory.get();
            var result = manager.append(new AddModifierToClipEvent(selectedClip.first(), selectedClip.second(), m));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to add modifier: " + result, ButtonType.OK).showAndWait();
            }
        });

        selectedClip.addListener((ignored1, ignored2, newValue) -> {
            if (newValue == null) {
                modifierList.getChildren().clear();
                return;
            }
            var clip = manager.get().getTrack(newValue.first()).flatMap(t -> t.getClip(newValue.second())).orElseThrow();
            refreshModifierList(newValue.first(), clip);
        });

        changeCallback = manager.registerChangeCallback(target -> {
            var timeline = target.getTimeline();
            var selected = selectedClip.get();

            // Update modifier list if possible
            if (target.isClip() && selected != null) {
                assert target.getTrackId().isPresent();
                assert target.getClipId().isPresent();
                var trackId = target.getTrackId().orElseThrow();
                var clipId = target.getClipId().orElseThrow();
                if (selected.first().equals(trackId) && selected.second().equals(clipId)) {
                    var clip = manager.get().getTrack(trackId).flatMap(t -> t.getClip(clipId)).orElseThrow();
                    refreshModifierList(trackId, clip);
                }
            }

            // Check for clip deletion
            if (target.isTrack()) {
                HashSet<UUID> deleted = new HashSet<>(knownClips);
                knownClips.clear();
                for (Track track : timeline) {
                    for (Clip clip : track) {
                        deleted.remove(clip.getId());
                        knownClips.add(clip.getId());
                    }
                }

                // Hide editor if open clip was deleted
                if (deleted.contains(lastOpenEditor)) {
                    setupEditorFor(null, null);
                }

                // Clear modifier list if selected clip was deleted
                if (selected != null && deleted.contains(selected.second())) {
                    refreshModifierList(null, null);
                }
            }

            // Check for track addition/deletion
            if (target.isTimeline()) {

                var tracks = trackList.getChildren();

                ArrayList<UUID> newTracks = new ArrayList<>();
                for (int i = 0; i < timeline.size(); i++) {
                    var newId = timeline.getTrack(i).getId();
                    newTracks.add(newId);

                    boolean add;
                    if (i < knownTracks.size()) {
                        var oldId = knownTracks.get(i);
                        if (!oldId.equals(newId)) {
                            // Replace node (make sure to call close if possible)
                            if (tracks.remove(i) instanceof AutoCloseable closeable) {
                                try {
                                    closeable.close();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            add = true; // Replace node
                        } else {
                            add = false; // Same track ID, do nothing
                        }
                    } else {
                        add = true; // Outside of known track list; must add a new node
                    }

                    // Add new node
                    if (add) {
                        tracks.add(i, TrackSubScene.create(manager, newId, this::trackCallback));
                    }
                }

                // Remove extras
                if (newTracks.size() < knownTracks.size()) {
                    tracks.remove(newTracks.size(), knownTracks.size());
                }

                knownTracks.clear();
                knownTracks.addAll(newTracks);
            }
        });
    }

    private void setupEditorFor(UUID trackId, Clip clip) {
        lastOpenEditor = Optional.ofNullable(clip).map(Clip::getId).orElse(null);

        if (trackId == null || clip == null) {
            // ClipEditorSubscene implements AutoClosable, make sure to call close() on it!
            if (editorPane.getContent() instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            editorPane.setContent(null);
            editorPane.setExpanded(false);
            return;
        }

        SubScene scene;
        switch (clip) {
            case PianoRollClip c -> scene = PianoRollEditorSubScene.create(manager, trackId, c.getId());
            case ChordClip c -> scene = ChordClipEditorSubScene.create(manager, trackId, c.getId());
            case ScaleClip c -> scene = ScaleClipEditorSubScene.create(manager, trackId, c.getId());
            default -> throw new IllegalArgumentException("Unknown clip type: " + clip.getClass().getName());
        }

        scene.widthProperty().bind(editorPane.prefWidthProperty());
        scene.heightProperty().bind(root.heightProperty().multiply(0.5));

        // ClipEditorSubscene implements AutoClosable, make sure to call close() on it!
        if (editorPane.getContent() instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        editorPane.setContent(scene);
        editorPane.setExpanded(true);
    }

    @FXML
    private void onAddTrackButtonClicked() {
        addTrack(new Track());
    }


    @FXML
    private void onBpmScrolled(ScrollEvent event) {
        if (Math.abs(event.getDeltaY()) < 1e-6) {
            return;
        }
        int delta = event.getDeltaY() >= 0 ? 1 : -1;
        bpm.set(bpm.get() + delta);
    }

    private double bpmDragStartY;
    private int bpmDragStartValue;
    @FXML
    private void onBpmDragged(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            bpmDragStartY = event.getScreenY();
            bpmDragStartValue = bpm.get();
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            bpm.set((int) (bpmDragStartValue - (0.25 * (event.getScreenY() - bpmDragStartY))));
        }
    }

    private void addTrack(@NotNull Track track) {
        var result = manager.append(new AddTrackToTimelineEvent(track));
        if (!result.isOk()) {
            new Alert(Alert.AlertType.ERROR, "Failed to add track: " + result, ButtonType.OK).showAndWait();
        }
    }

    private void trackCallback(UUID trackId, TrackCallbackType type) {
        switch (type) {
            case DELETE -> {
                var result = manager.append(new RemoveTrackFromTimelineEvent(trackId));
                if (!result.isOk()) {
                    new Alert(Alert.AlertType.ERROR, "Failed to add track: " + result, ButtonType.OK).showAndWait();
                }
            }
            case null, default -> throw new IllegalArgumentException();
        }
    }

    private void timelineCallback(@Nullable UUID trackId, @Nullable UUID clipId, TimelineRenderer.CallbackType type) {
        switch (type) {
            case OPEN_EDITOR -> {
                assert trackId != null;
                assert clipId != null;
                setupEditorFor(trackId, manager.get().getClip(clipId).orElseThrow());
            }
            case CLIP_SELECTION_CHANGED -> {
                if (trackId != null && clipId != null) {
                    selectedClip.set(new Tuple2<>(trackId, clipId));
                } else {
                    selectedClip.set(null);
                }
            }
            case null, default -> throw new IllegalArgumentException();
        }
    }

    private void refreshModifierList(UUID trackId, Clip clip) {
        var children = modifierList.getChildren();
        for (Node child : children) {
            if (child instanceof TitledPane pane && pane.getContent() instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        children.clear();
        if (trackId != null && clip != null) {
            for (Modifier m : clip.getModifiers()) {
                children.add(createModifierNodeFor(trackId, clip, m));
            }
        }
    }

    private TitledPane createModifierNodeFor(UUID trackId, Clip clip, Modifier modifier) {
        // Setup label + buttons
        HBox title = new HBox();

        Label label = new Label(MODIFIER_LABELS.get2(modifier.getClass()));
        title.getChildren().add(new AnchorPane(label));
        // Right aligns the buttons
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        HBox.setHgrow(label.getParent(), Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            var result = manager.append(new RemoveModifierFromClipEvent(trackId, clip.getId(), modifier.getId()));
            if (!result.isOk()) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete modifier: " + result, ButtonType.OK);
            }
        });
        title.getChildren().add(deleteButton);

        TitledPane pane = new TitledPane();

        // Fix width
        pane.setAnimated(false);
        pane.prefWidthProperty().bind(modifierList.prefWidthProperty());
        pane.maxWidthProperty().bind(pane.prefWidthProperty());
        pane.minWidthProperty().bind(pane.prefWidthProperty());

        // Add label and buttons
        pane.setGraphic(title);
        pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        var left = DoubleExpression.doubleExpression(title.boundsInParentProperty().map(Bounds::getMinX));
        title.prefWidthProperty().bind(modifierList.prefWidthProperty().subtract(left));

        var subscene = MODIFIER_EDITOR_FACTORIES.get(modifier.getClass()).create(manager, trackId, clip.getId(), modifier.getId());
        subscene.widthProperty().bind(pane.widthProperty());
        pane.setContent(subscene);

        return pane;
    }

    @Override
    public void close() throws Exception {
        changeCallback.close();
    }
}
