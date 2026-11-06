package me.aap.fermata.ui.voice;

import androidx.annotation.Nullable;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Small deterministic grammar for common car commands. It deliberately does not use an LLM
 * and returns null when the utterance is ambiguous or outside the supported grammar.
 */
public final class VoiceIntentParser {
	private static final Map<String, String> ADDON_ALIASES = new LinkedHashMap<>();
	// Longer prefixes first, so that "mets-moi " is removed whole rather than as "mets ".
	private static final String[] PLAY_PREFIXES = {"play ", "phát ", "phat ", "oynat ",
			"mets-moi ", "mets moi ", "joue-moi ", "joue moi ", "mets ", "joue ", "écoute ", "ecoute ",
			"lecture de ", "lecture "};
	private static final String[] FIND_PREFIXES = {"find ", "search ", "tìm kiếm ", "tim kiem ",
			"tìm ", "tim ", "ara ", "bul ", "recherche ", "cherche ", "trouve "};
	private static final String[] OPEN_PREFIXES = {"open ", "mở ", "mo ", "aç ", "ouvre ", "ouvrir ",
			"lance ", "va sur ", "affiche "};

	static {
		alias("youtube", "youtube", "you tube", "yt", "youtube’da", "youtube\'da");
		alias("tv", "tv", "television", "tivi", "truyền hình", "truyen hinh",
				"xtream", "xtreme", "iptv", "televizyon",
				"télé", "tele", "télévision", "la télé", "la tele", "la télévision", "les chaînes", "les chaines");
		alias("radio", "radio", "đài", "dai", "radyo", "la radio");
		alias("stremio", "stremio");
		alias("podcast", "podcast", "podcasts", "les podcasts", "le podcast");
		alias("audiobook", "audiobook", "audiobooks", "sách nói", "sach noi", "sesli kitap",
				"livre audio", "livres audio", "les livres audio");
		alias("web", "web", "browser", "trình duyệt", "trinh duyet", "tarayıcı", "tarayici",
				"navigateur", "le navigateur", "internet");
		alias("chatgpt", "chatgpt", "chat gpt", "gpt", "assistant", "trợ lý", "tro ly");
	}

	private VoiceIntentParser() {
	}

	private static void alias(String canonical, String... values) {
		for (String value : values) ADDON_ALIASES.put(value, canonical);
	}

	@Nullable
	public static VoiceIntent parse(@Nullable String phrase, @Nullable Locale locale) {
		String text = normalize((phrase != null && locale != null && "tr".equals(locale.getLanguage())) ?
				phrase.toLowerCase(locale) : phrase);
		if (text.isEmpty()) return null;

		if (isAny(text, "play", "phát", "phat", "resume", "tiếp tục", "tiep tuc", "oynat", "başlat", "devam et",
				"lecture", "joue", "reprends", "reprendre", "continue"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PLAY);
		if (isAny(text, "pause", "tạm dừng", "tam dung", "duraklat", "mets en pause"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PAUSE);
		if (isAny(text, "stop", "dừng", "dung", "durdur", "arrête", "arrete", "arrêter", "coupe"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.STOP);
		if (isAny(text, "next", "next track", "next item", "bài tiếp theo", "bai tiep theo",
				"kênh tiếp theo", "kenh tiep theo", "tập tiếp theo", "tap tiep theo", "sonraki", "sonraki kanal", "sonraki bölüm", "sonraki parça",
				"suivant", "suivante", "chaîne suivante", "chaine suivante", "station suivante",
				"épisode suivant", "episode suivant", "morceau suivant", "titre suivant"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.NEXT);
		if (isAny(text, "previous", "previous track", "previous item", "bài trước",
				"bai truoc", "kênh trước", "kenh truoc", "tập trước", "tap truoc", "önceki", "önceki kanal", "önceki bölüm", "önceki parça",
				"précédent", "precedent", "précédente", "precedente", "chaîne précédente", "chaine precedente",
				"station précédente", "station precedente", "épisode précédent", "episode precedent",
				"morceau précédent", "morceau precedent", "titre précédent", "titre precedent"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PREVIOUS);
		if (isAny(text, "go back", "quay lại", "quay lai", "geri", "geri dön", "retour", "reviens"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.BACK);
		if (isAny(text, "what is playing", "now playing", "đang phát gì", "dang phat gi", "ne çalıyor", "şu an oynatılan",
				"qu'est-ce qui passe", "qu’est-ce qui passe", "qu'est ce qui passe", "c'est quoi ce titre", "morceau en cours"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.OPEN_CURRENT);
		if (isAny(text, "play favorites", "play my favorites", "phát mục yêu thích",
				"phat muc yeu thich", "favorileri oynat", "favorilerimi oynat", "oynat favoriler",
				"joue mes favoris", "mets mes favoris", "joue les favoris", "mets les favoris"))
			return VoiceIntent.playback(VoiceIntent.PlaybackAction.PLAY_FAVORITES);

		int selection = parseSelectionIndex(text);
		if (selection >= 0) return VoiceIntent.selection(selection);

		// Turkish commonly places the verb after the content: "YouTube aç", "TV TRT 1 oynat".
		// Keep an already explicit action prefix intact, including English queries ending in a Turkish verb.
		if (locale != null && "tr".equals(locale.getLanguage()) && parseSearchAction(text) == null && !text.startsWith("altyazı")) {
			for (String verb : new String[]{"aç", "oynat", "ara", "bul"}) {
				if (text.endsWith(" " + verb)) {
					text = verb + " " + text.substring(0, text.length() - verb.length()).trim();
					break;
				}
			}
		}
		VoiceIntent.SearchAction action = parseSearchAction(text);
		if (action == null) return null;
		String remainder = removeAction(text, action);
		if (remainder.isEmpty()) return null;

		String addon = null;
		// French: "cherche sur YouTube …", "mets dans la radio …".
		for (String prep : new String[]{"sur ", "dans "}) {
			if (!remainder.startsWith(prep)) continue;
			String rest = remainder.substring(prep.length());
			for (String alias : ADDON_ALIASES.keySet()) {
				if (rest.equals(alias) || rest.startsWith(alias + " ")) {
					remainder = rest;
					break;
				}
			}
			break;
		}
		for (Map.Entry<String, String> e : ADDON_ALIASES.entrySet()) {
			String alias = e.getKey();
			if (remainder.equals(alias) || remainder.startsWith(alias + " ")) {
				addon = e.getValue();
				remainder = remainder.substring(alias.length()).trim();
				break;
			}
		}

		if ((action == VoiceIntent.SearchAction.OPEN) && (addon != null) && remainder.isEmpty()) {
			return VoiceIntent.openAddon(addon);
		}
		remainder = removeContentQualifier(remainder);
		return remainder.isEmpty() ? null : VoiceIntent.search(action, addon, remainder);
	}

	/** Converts a MediaSession search query into the same deterministic in-app grammar. */
	@Nullable
	public static String mediaSearchCommand(@Nullable String query, @Nullable Locale locale) {
		if ((query == null) || query.isBlank()) return null;
		String command = query.trim();
		if (parse(command, locale) != null) return command;
		command = "play " + command;
		return (parse(command, locale) == null) ? null : command;
	}

	public static int parseSelectionIndex(@Nullable String phrase) {
		String text = normalize(phrase);
		if (text.isEmpty()) return -1;
		if (isAny(text, "1", "one", "first", "number one", "result one", "số một",
				"so mot", "kết quả một", "ket qua mot", "bir", "birinci", "birinci sonuç",
				"un", "le premier", "premier", "première", "la première", "numéro un", "numero un"))
			return 0;
		if (isAny(text, "2", "two", "second", "number two", "result two", "số hai",
				"so hai", "kết quả hai", "ket qua hai", "iki", "ikinci", "ikinci sonuç",
				"deux", "le deuxième", "deuxième", "deuxieme", "second", "seconde", "numéro deux", "numero deux"))
			return 1;
		if (isAny(text, "3", "three", "third", "number three", "result three", "số ba",
				"so ba", "kết quả ba", "ket qua ba", "üç", "üçüncü", "üçüncü sonuç",
				"trois", "le troisième", "troisième", "troisieme", "numéro trois", "numero trois"))
			return 2;
		return -1;
	}

	private static VoiceIntent.SearchAction parseSearchAction(String text) {
		if (startsWithAny(text, PLAY_PREFIXES))
			return VoiceIntent.SearchAction.PLAY;
		if (startsWithAny(text, FIND_PREFIXES))
			return VoiceIntent.SearchAction.FIND;
		if (startsWithAny(text, OPEN_PREFIXES))
			return VoiceIntent.SearchAction.OPEN;
		return null;
	}

	private static String removeAction(String text, VoiceIntent.SearchAction action) {
		String[] prefixes = switch (action) {
			case PLAY -> PLAY_PREFIXES;
			case FIND -> FIND_PREFIXES;
			case OPEN -> OPEN_PREFIXES;
		};
		for (String prefix : prefixes) if (text.startsWith(prefix)) return text.substring(prefix.length()).trim();
		return text;
	}

	private static String removeContentQualifier(String text) {
		String[] qualifiers = {"video ", "channel ", "kênh ", "kenh ", "bài ", "bai ",
				"movie ", "film ", "phim ", "series ",
				"episode ", "tập ", "tap ", "book ", "sách ", "sach ", "kanal ", "bölüm ", "kitap ",
				"la chaîne ", "la chaine ", "chaîne ", "chaine ", "la station ", "station ", "l'émission ",
				"l’émission ", "émission ", "la vidéo ", "vidéo ", "le film ", "l'épisode ", "l’épisode ",
				"le podcast ", "la radio ", "radio "};
		for (String qualifier : qualifiers) if (text.startsWith(qualifier)) return text.substring(qualifier.length()).trim();
		return text;
	}

	private static boolean startsWithAny(String text, String... prefixes) {
		for (String prefix : prefixes) if (text.startsWith(prefix)) return true;
		return false;
	}

	private static boolean isAny(String text, String... values) {
		for (String value : values) if (text.equals(value)) return true;
		return false;
	}

	private static String normalize(String phrase) {
		if (phrase == null) return "";
		String text = Normalizer.normalize(phrase, Normalizer.Form.NFKC).trim().toLowerCase(Locale.ROOT);
		text = text.replaceAll("[,.!?;:]+", " ").replaceAll("\\s+", " ").trim();
		return text;
	}
}
