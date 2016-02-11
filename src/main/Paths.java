package main;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.jetbrains.annotations.Nullable;
import data.Documentation;
import data.Type;
import exceptions.ErrorMessage;
import exceptions.ErrorMessageType;
import xml.XML;


abstract public class Paths
{
	static public final Path USER_DIR = java.nio.file.Paths.get(System.getProperty("user.dir"));
	static public final Path DATA_DIR = USER_DIR.resolve("data");
	static public final String XML = ".xml";
	static public final String TTF = ".ttf";
	static public final String JAR_ROOT = "/";
	static public final String RESOURCES = JAR_ROOT + "res/";
	static public final String FONTS = RESOURCES + "ttf/";
	static public final String IMAGES = RESOURCES + "png/";


	/**
	 * Contrôle le répertoire data. S'il n'existe pas l'application quitte.
	 *
	 * @return true si le répertoire data est accessible, sinon false
	 */
	static public boolean CHECK_DATA_DIRECTORY()
	{
		if (Files.notExists(DATA_DIR) || !Files.isDirectory(DATA_DIR))
			Launcher.INSTANCE.raise(new ErrorMessage(ErrorMessageType.directoryNotFound, DATA_DIR));
		if (Files.isReadable(DATA_DIR)) return true;
		Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.directoryAccessDenied, DATA_DIR));
		return false;
	}


	/**
	 * Contrôle un répertoire.
	 *
	 * @param directory Chemin d'un répertoire
	 * @return true si le répertoire existe et est accessible, sinon false
	 */
	static public boolean CHECK_DIRECTORY(@Nullable Path directory)
	{
		if (directory == null || Files.notExists(directory) || !Files.isDirectory(directory)) return false;
		if (Files.isReadable(directory)) return true;
		Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.directoryAccessDenied, directory));
		return false;
	}


	/**
	 * Contrôle un fichier.
	 *
	 * @param file Chemin d'un fichier
	 * @param access Contrôler également l'accessibilité
	 * @return true si le fichier existe, sinon false
	 */
	static public boolean CHECK_FILE(@Nullable Path file, boolean access)
	{
		if (file == null || Files.notExists(file) || Files.isDirectory(file)) return false;
		if (!access || Files.isReadable(file)) return true;
		Launcher.INSTANCE.log(new ErrorMessage(ErrorMessageType.fileAccessDenied, file));
		return false;
	}


	/**
	 * Récupére le chemin d'un répertoire de classe.
	 *
	 * @param type Type
	 * @return Un objet Path ou null si le répertoire de classe n'existe pas ou n'est pas accessible.
	 */
	@Nullable
	static public Path GET_CLASS_PATH(@Nullable Type type)
	{
		if (!CHECK_DATA_DIRECTORY() || type == null) return null;
		Path p = DATA_DIR.resolve(type.getName());
		return CHECK_DIRECTORY(p) ? p : null;
	}


	/**
	 * Récupére un objet XML depuis un fichier XML.
	 *
	 * @param xmlFilePath Chemin du fichier
	 * @return Un objet XML ou null si le fichier n'existe pas ou n'est pas accessible
	 */
	@Nullable
	static public XML OPEN(@Nullable Path xmlFilePath)
	{
		if (!CHECK_FILE(xmlFilePath, true)) return null;

		try
		{
			InputStream s = Files.newInputStream(xmlFilePath, StandardOpenOption.READ);
			XML x = new XML(Documentation.BUILDER.parse(s).getDocumentElement());
			s.close();
			return x;
		}
		catch (Exception e)
		{
			Launcher.INSTANCE.log(e);
		}

		return null;
	}
}