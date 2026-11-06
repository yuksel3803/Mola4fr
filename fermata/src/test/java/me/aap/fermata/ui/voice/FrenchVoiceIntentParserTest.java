package me.aap.fermata.ui.voice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;

public class FrenchVoiceIntentParserTest {
	private static final Locale FR = Locale.FRENCH;

	@Test
	public void frenchPlaybackCommands() {
		assertEquals(VoiceIntent.PlaybackAction.PAUSE, VoiceIntentParser.parse("Pause", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.PAUSE, VoiceIntentParser.parse("mets en pause", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.PLAY, VoiceIntentParser.parse("reprends", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.STOP, VoiceIntentParser.parse("arrête", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.NEXT, VoiceIntentParser.parse("Chaîne suivante", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.PREVIOUS, VoiceIntentParser.parse("station précédente", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.BACK, VoiceIntentParser.parse("retour", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.OPEN_CURRENT,
				VoiceIntentParser.parse("qu'est-ce qui passe ?", FR).getPlaybackAction());
		assertEquals(VoiceIntent.PlaybackAction.PLAY_FAVORITES,
				VoiceIntentParser.parse("joue mes favoris", FR).getPlaybackAction());
	}

	@Test
	public void frenchOpenAndSearchCommands() {
		VoiceIntent youtube = VoiceIntentParser.parse("Ouvre YouTube", FR);
		assertEquals(VoiceIntent.Kind.OPEN_ADDON, youtube.getKind());
		assertEquals("youtube", youtube.getAddon());
		assertEquals("radio", VoiceIntentParser.parse("lance la radio", FR).getAddon());
		assertEquals("tv", VoiceIntentParser.parse("ouvre la télé", FR).getAddon());

		VoiceIntent station = VoiceIntentParser.parse("Mets la radio France Inter", FR);
		assertEquals(VoiceIntent.SearchAction.PLAY, station.getSearchAction());
		assertEquals("radio", station.getAddon());
		assertEquals("france inter", station.getQuery());

		VoiceIntent channel = VoiceIntentParser.parse("mets la chaîne France 2", FR);
		assertEquals(VoiceIntent.SearchAction.PLAY, channel.getSearchAction());
		assertEquals("france 2", channel.getQuery());

		VoiceIntent search = VoiceIntentParser.parse("cherche sur YouTube", FR);
		assertNull(search);
		VoiceIntent video = VoiceIntentParser.parse("cherche YouTube recette crêpes", FR);
		assertEquals(VoiceIntent.SearchAction.FIND, video.getSearchAction());
		assertEquals("youtube", video.getAddon());
		assertEquals("recette crêpes", video.getQuery());
	}

	@Test
	public void frenchSelection() {
		assertEquals(0, VoiceIntentParser.parseSelectionIndex("le premier"));
		assertEquals(1, VoiceIntentParser.parseSelectionIndex("deuxième"));
		assertEquals(2, VoiceIntentParser.parseSelectionIndex("numéro trois"));
		assertNull(VoiceIntentParser.parse("quel temps fait-il", FR));
	}

	@Test
	public void frenchGrammarCompiles() throws Exception {
		Path root = Path.of(System.getProperty("user.dir"));
		if (!Files.isDirectory(root.resolve("fermata"))) root = root.getParent();
		var nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				root.resolve("fermata/src/main/res/values-fr/voice_cmd.xml").toFile())
				.getElementsByTagName("string");
		Map<String, Pattern> patterns = new HashMap<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			var node = nodes.item(i);
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			if (name.equals("vcmd_nums")) continue;
			patterns.put(name, Pattern.compile(node.getTextContent()
					.replace("\\\\", "\\").replace("\\'", "'")));
		}
		var forward = patterns.get("vcmd_ff").matcher("avance de 30 secondes");
		assertTrue(forward.matches());
		assertEquals("30", forward.group("N"));
		var backward = patterns.get("vcmd_rw").matcher("recule de deux minutes");
		assertTrue(backward.matches());
		assertEquals("deux", backward.group("N"));
		assertTrue(patterns.get("vcmd_action_sub_on").matcher("active les sous-titres").matches());
		assertTrue(patterns.get("vcmd_action_play_favorites").matcher("joue mes favoris").matches());
		var open = patterns.get("vcmd_find_play_open").matcher("mets la télé France 2");
		assertTrue(open.matches());
		assertEquals("mets", open.group("A"));
		assertEquals("télé", open.group("L"));
		assertEquals("France 2", open.group("Q"));
	}
}
