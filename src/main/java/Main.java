import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptParser;
import io.github.turtleisaac.pokeditor.formats.scripts.TrainerAiData;
import io.github.turtleisaac.pokeditor.formats.scripts.TrainerAiParser;
import io.github.turtleisaac.pokeditor.gamedata.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class Main {
	public static void main(String[] args)
	{
		NintendoDsRom rom = NintendoDsRom.fromFile("Platinum.nds");
		Game game = Game.parseBaseRom(rom.getGameCode());
		GameFiles.initialize(game);
		TextFiles.initialize(game);
		GameCodeBinaries.initialize(game);
		Tables.initialize(game);

		byte[] ai_file = rom.getFileByName(GameFiles.TR_AI.getPath());
		byte[] scripts_file = rom.getFileByName(GameFiles.SCRIPTS.getPath());
		System.out.println("Read " + ai_file.length + " bytes from " + GameFiles.TR_AI.getPath());
		System.out.println("Read " + scripts_file.length + " bytes from " + GameFiles.SCRIPTS.getPath());

		Map<GameFiles, Narc> narcMap = new HashMap<>();
		narcMap.put(GameFiles.TR_AI, new Narc(ai_file));
		narcMap.put(GameFiles.SCRIPTS, new Narc(scripts_file));

		CodeBinary arm9 = rom.loadArm9();
		Map<GameCodeBinaries, CodeBinary> codeBinaries = Collections.singletonMap(GameCodeBinaries.ARM9, arm9);

		TrainerAiParser parser = new TrainerAiParser();
		//ScriptParser parser2 = new ScriptParser();

		List<GenericScriptData> scripts = parser.generateDataList(narcMap, codeBinaries);
		System.out.println("Found " + scripts.size() + " scripts.");
		scripts.forEach(script -> {
			TrainerAiData data = (TrainerAiData)script;
			System.out.println("Script size: " + data.getScripts().size());
			System.out.print(script.toString());
		});
	}
}
