package window;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import events.KeyboardManager;
import fonts.FontManager;
import ui.ConstraintFactory;
import ui.Frame;
import ui.Graphics;


public class ErrorWindow extends Stage
{
	static private final byte _PADDING = 4;
	static private final byte _BUTTON_HEIGHT = 25;
	static private final byte _BORDER = 1;
	static private final byte _MARGIN = 8;

	protected Frame frame;
	protected Text content;
	private AnchorPane _buttons;


	public ErrorWindow(Stage owner, Modality modality, short width, short height, String title, String message)
	{
		super();
		super.initOwner(owner);
		super.initModality(modality);
		super.setResizable(false);
		super.setTitle(title);
		KeyboardManager.GET_MANAGER(this);

		// TODO texte sélectionnable
		GridPane g = new GridPane();
		g.setPadding(new Insets(_PADDING));

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.ALWAYS);
		RowConstraints r0 = new RowConstraints();
		r0.setVgrow(Priority.ALWAYS);
		RowConstraints r1 = ConstraintFactory.GET_FIXED_ROW(_PADDING);
		RowConstraints r2 = ConstraintFactory.GET_FIXED_ROW(_BUTTON_HEIGHT);
		r2.setValignment(VPos.CENTER);

		g.getColumnConstraints().add(c0);
		g.getRowConstraints().addAll(r0, r1, r2);

		Region bg = new Region();
		BackgroundFill f1 = new BackgroundFill(Color.gray(0.4), CornerRadii.EMPTY, Insets.EMPTY);
		BackgroundFill f2 = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, new Insets(_BORDER));
		bg.setBackground(new Background(f1, f2));
		g.add(bg, 0, 0);

		frame = new Frame(new Insets(4));
		g.add(frame, 0, 0);
		GridPane.setMargin(frame, new Insets(Graphics.THIN));

		content = new Text(message);
		content.setTextOrigin(VPos.TOP);
		Pane p = new Pane(); // Frame attends une Region
		Font f = FontManager.INSTANCE.getFont(FontManager.MONO_SPACE_FAMILY_NAME, (byte) 1);
		if (f == null) FontManager.INSTANCE.getFont();
		if (f == null) f = Font.getDefault();
		content.setFont(f);
		p.getChildren().add(content);
		frame.setContent(p);

		Button b = new Button("OK");
		_buttons = new AnchorPane(b);
		AnchorPane.setRightAnchor(b, 0d);
		g.add(_buttons, 0, 2);

		super.setScene(new Scene(g, width, height, Graphics.BACKGROUND_COLOR));
		final ErrorWindow w = this; // accès dans le handler

		b.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				w.hide();
			}
		});
	}


	protected void setLeftAnchor(Node child)
	{
		if (child == null) return;
		_buttons.getChildren().add(child);
		AnchorPane.setLeftAnchor(child, 0d);
	}
}