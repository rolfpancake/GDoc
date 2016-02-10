package ui;


import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import fonts.FontManager;
import main.Paths;


abstract public class Graphics
{
	static public final Color BACKGROUND_COLOR = Color.gray(240d / 255);
	static public final Color HEADER_COLOR = Color.gray(230d / 255);
	static public final Color TABS_BACKGROUND_COLOR = Color.gray(200d / 255);
	static public final Color BLUE = Color.web("0x3E79A6");
	static public final Color INVALID_ID = Color.web("0xC00000");
	static public final Color TEXT_COLOR = Color.gray(50d / 255);
	static public final Color GRAY_2 = Color.gray(20d / 255);
	static public final Color GRAY_5 = Color.gray(50d / 255);
	static public final Color GRAY_6 = Color.gray(60d / 255);
	static public final Color GRAY_8 = Color.gray(80d / 255);
	static public final Color GRAY_9 = Color.gray(90d / 255);
	static public final Color GRAY_10 = Color.gray(100d / 255);
	static public final Color GRAY_12 = Color.gray(120d / 255);
	static public final Color GRAY_13 = Color.gray(130d / 255);
	static public final Color GRAY_15 = Color.gray(150d / 255);
	static public final Color GRAY_16 = Color.gray(160d / 255);
	static public final Color GRAY_19 = Color.gray(190d / 255);
	static public final Color GRAY_20 = Color.gray(200d / 255);
	static public final Color GRAY_21 = Color.gray(210d / 255);
	static public final Color GRAY_22 = Color.gray(220d / 255);
	static public final Color GRAY_23 = Color.gray(230d / 255);
	static public final Color GRAY_24 = Color.gray(240d / 255);
	static public final Color HIGHLIGHT = Color.web("0xD941D9");
	static public final Color PSEUDO_VALUE_COLOR = Color.gray(130d / 255);

	static public final byte THIN = 1;
	static public final byte DOUBLE = 2;
	static public final byte PADDING = 4;
	static public final byte CORNER = 4;

	static public final Background BACKGROUND = COLOR_BACKGROUND(BACKGROUND_COLOR);
	static public final Background HEADER_BACKGROUND = GRAY_BACKGROUND((short) 230);

	static public final byte ICON_SIZE_24 = 24;
	static public final byte ICON_SIZE_16 = 16;
	static public final byte ICONS_PER_ROW = 5;
	static public final ColorAdjust FULL_LIGHT_EFFECT = new ColorAdjust(0, 0, 1.0, 0);

	static private final Image _ICONS24 = new Image((Object.class).getResourceAsStream(Paths.IMAGES + "icons24.png"));
	static private final Image _ICONS16 = new Image((Object.class).getResourceAsStream(Paths.IMAGES + "icons16.png"));


	static public Background COLOR_BACKGROUND(Color color)
	{
		return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
	}


	static public Background DEBUG_BACKGROUND(short hue)
	{
		return COLOR_BACKGROUND(Color.hsb(hue < 0 ? Math.random() * 360 : hue, 0.2, 0.85));
	}


	static public Background GRAY_BACKGROUND(short light)
	{
		return COLOR_BACKGROUND(Color.gray((double) Math.max(0, Math.min(light, 255)) / 255));
	}


	static public Background RANDOM_BACKGROUND()
	{
		return COLOR_BACKGROUND(Color.color(Math.random(), Math.random(), Math.random()));
	}


	static public Text CREATE_TEXT_FIELD(String text)
	{
		Text f = new Text(text);
		f.setTextOrigin(VPos.TOP);
		f.setFill(TEXT_COLOR);
		f.setFont(FontManager.INSTANCE.getFont());
		f.setMouseTransparent(true);
		return f;
	}


	static public Text CREATE_TEXT_FIELD(String text, Font font)
	{
		Text f = new Text(text);
		f.setTextOrigin(VPos.TOP);
		f.setFill(TEXT_COLOR);
		f.setFont(font != null ? font : FontManager.INSTANCE.getFont());
		f.setMouseTransparent(true);
		return f;
	}


	static public ImageView GET_ICON_16(byte column, byte row)
	{
		ImageView i = new ImageView(_ICONS16);
		i.setViewport(new Rectangle2D(ICON_SIZE_16 * Math.max(0, Math.min(column, ICONS_PER_ROW)),
									  ICON_SIZE_16 * Math.max(0, Math.min(row, ICONS_PER_ROW)),
									  ICON_SIZE_16, ICON_SIZE_16));
		i.setSmooth(true);
		return i;
	}


	static public ImageView GET_ICON_24(byte column, byte row)
	{
		ImageView i = new ImageView(_ICONS24);
		i.setViewport(new Rectangle2D(ICON_SIZE_24 * Math.max(0, Math.min(column, ICONS_PER_ROW)),
									  ICON_SIZE_24 * Math.max(0, Math.min(row, ICONS_PER_ROW)),
									  ICON_SIZE_24, ICON_SIZE_24));
		i.setSmooth(true);
		return i;
	}


	static public double TEXT_SIZE(String string, Font font)
	{
		if (string == null || string.isEmpty()) return 0;
		return Toolkit.getToolkit().getFontLoader().computeStringWidth(string, font);
	}
}