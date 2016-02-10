package settings;

interface ISetting
{
	String INVALID_NAME = "invalid name";

	String toIniString();
}