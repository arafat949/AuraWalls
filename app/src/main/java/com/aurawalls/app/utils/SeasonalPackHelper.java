package com.aurawalls.app.utils;
import java.util.Calendar;
public class SeasonalPackHelper {
    public static class SeasonalTheme {
        public final String name, emoji, query, message;
        SeasonalTheme(String n, String e, String q, String m) {
            name=n; emoji=e; query=q; message=m;
        }
    }
    public static SeasonalTheme getActiveTheme() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int day   = cal.get(Calendar.DAY_OF_MONTH);

        // ══════════════════════════════════════════
        // BANGLADESH / SOUTH ASIA
        // ══════════════════════════════════════════
        if (month == 4 && day >= 11 && day <= 17)
            return new SeasonalTheme("Pohela Boishakh","🎊",
                "colorful festival bengali new year","Happy Bengali New Year! 🎉");
        if (month == 2 && day >= 20 && day <= 22)
            return new SeasonalTheme("Language Day","🌹",
                "red roses memorial tribute elegant","International Mother Language Day 🌹");
        if (month == 3 && day >= 25 && day <= 27)
            return new SeasonalTheme("Independence Day BD","🇧🇩",
                "bangladesh flag green red proud","Happy Independence Day, Bangladesh! 🇧🇩");
        if (month == 12 && day >= 15 && day <= 17)
            return new SeasonalTheme("Victory Day BD","🏆",
                "victory celebration bangladesh pride","Happy Victory Day, Bangladesh! 🏆");

        // ══════════════════════════════════════════
        // ISLAMIC FESTIVALS (approximate dates 2026)
        // ══════════════════════════════════════════
        if (month == 3 && day >= 20 && day <= 22)
            return new SeasonalTheme("Eid ul-Fitr","🌙",
                "eid mubarak crescent moon mosque","Eid Mubarak! 🕌");
        if ((month == 5 && day >= 27) || (month == 6 && day <= 1))
            return new SeasonalTheme("Eid ul-Adha","🐑",
                "eid adha prayer sunrise golden","Eid ul-Adha Mubarak! 🌅");
        if (month == 3 && day >= 1 && day <= 3)
            return new SeasonalTheme("Ramadan Begins","🌙",
                "ramadan kareem moon night prayer","Ramadan Mubarak! ✨");

        // ══════════════════════════════════════════
        // HINDU FESTIVALS
        // ══════════════════════════════════════════
        if (month == 10 && day >= 1 && day <= 10)
            return new SeasonalTheme("Durga Puja","🪔",
                "durga puja festival lights indian","Happy Durga Puja! 🎆");
        if ((month == 10 && day >= 20) || (month == 11 && day <= 5))
            return new SeasonalTheme("Diwali","✨",
                "diwali lights candles golden festival","Happy Diwali! 🪔");
        if (month == 3 && day >= 5 && day <= 8)
            return new SeasonalTheme("Holi","🎨",
                "holi colors powder festival india","Happy Holi! Festival of Colors 🌈");
        if (month == 1 && day >= 14 && day <= 16)
            return new SeasonalTheme("Makar Sankranti","🪁",
                "kite festival india sunrise golden","Happy Makar Sankranti! 🪁");
        if (month == 8 && day >= 16 && day <= 20)
            return new SeasonalTheme("Janmashtami","🦚",
                "krishna blue peacock flute temple","Happy Janmashtami! 🦚");
        if (month == 9 && day >= 5 && day <= 12)
            return new SeasonalTheme("Ganesh Chaturthi","🐘",
                "ganesh festival flowers colorful india","Happy Ganesh Chaturthi! 🐘");

        // ══════════════════════════════════════════
        // CHRISTIAN / WESTERN
        // ══════════════════════════════════════════
        if (month == 12 && day >= 20 && day <= 26)
            return new SeasonalTheme("Christmas","🎄",
                "christmas winter snow cozy holiday","Merry Christmas! ❄️");
        if ((month == 12 && day >= 31) || (month == 1 && day <= 3))
            return new SeasonalTheme("New Year","🎆",
                "new year fireworks celebration city night","Happy New Year! 🥂");
        if (month == 2 && day >= 11 && day <= 14)
            return new SeasonalTheme("Valentine's Day","❤️",
                "romantic love valentine roses heart pink","Happy Valentine's Day! 💕");
        if (month == 4 && day >= 3 && day <= 6)
            return new SeasonalTheme("Easter","🐣",
                "easter spring flowers pastel eggs","Happy Easter! 🌸");
        if (month == 10 && day >= 29 && day <= 31)
            return new SeasonalTheme("Halloween","🎃",
                "halloween spooky night pumpkin dark","Happy Halloween! 👻");
        if (month == 11 && day >= 26 && day <= 28)
            return new SeasonalTheme("Thanksgiving","🦃",
                "thanksgiving autumn leaves harvest golden","Happy Thanksgiving! 🍂");

        // ══════════════════════════════════════════
        // EAST ASIAN
        // ══════════════════════════════════════════
        if (month == 2 && day >= 17 && day <= 19)
            return new SeasonalTheme("Chinese New Year","🐉",
                "chinese new year dragon lantern red gold","Happy Chinese New Year! 🧧");
        if (month == 2 && day >= 1 && day <= 5)
            return new SeasonalTheme("Lunar New Year","🏮",
                "lunar new year lantern festival night","Happy Lunar New Year! 🏮");
        if (month == 9 && day >= 28 && day <= 30)
            return new SeasonalTheme("Mid-Autumn Festival","🥮",
                "moon festival lantern mooncake night","Happy Mid-Autumn Festival! 🌕");
        if (month == 4 && day >= 4 && day <= 6)
            return new SeasonalTheme("Qingming Festival","🌸",
                "spring cherry blossom nature peaceful","Qingming Festival 🌸");
        if (month == 7 && day >= 7 && day <= 7)
            return new SeasonalTheme("Tanabata","🎋",
                "tanabata bamboo star festival japan","Happy Tanabata! 🌟");
        if (month == 8 && day >= 13 && day <= 15)
            return new SeasonalTheme("Obon Festival","🏮",
                "obon festival japan lantern river night","Happy Obon! 🏮");

        // ══════════════════════════════════════════
        // JEWISH
        // ══════════════════════════════════════════
        if (month == 9 && day >= 22 && day <= 24)
            return new SeasonalTheme("Rosh Hashanah","🍎",
                "jewish new year honey apple golden","Shanah Tovah! 🍎");
        if (month == 12 && day >= 14 && day <= 16)
            return new SeasonalTheme("Hanukkah","🕎",
                "hanukkah menorah candles gold blue","Happy Hanukkah! 🕎");

        // ══════════════════════════════════════════
        // PERSIAN / NOWRUZ
        // ══════════════════════════════════════════
        if (month == 3 && day >= 20 && day <= 22)
            return new SeasonalTheme("Nowruz","🌱",
                "nowruz persian new year spring flowers","Happy Nowruz! New Year 🌸");

        // ══════════════════════════════════════════
        // AFRICAN / CARIBBEAN
        // ══════════════════════════════════════════
        if (month == 12 && day >= 26 && day <= 31)
            return new SeasonalTheme("Kwanzaa","🕯️",
                "kwanzaa candles african heritage unity","Happy Kwanzaa! 🕯️");

        // ══════════════════════════════════════════
        // INTERNATIONAL DAYS
        // ══════════════════════════════════════════
        if (month == 3 && day >= 8 && day <= 8)
            return new SeasonalTheme("Women's Day","💜",
                "women empowerment purple flowers strong","Happy Women's Day! 💜");
        if (month == 4 && day >= 22 && day <= 22)
            return new SeasonalTheme("Earth Day","🌍",
                "earth nature green environment forest","Happy Earth Day! 🌍");
        if (month == 6 && day >= 5 && day <= 5)
            return new SeasonalTheme("World Environment Day","🌿",
                "environment nature green trees ocean","World Environment Day 🌿");
        if (month == 6 && day >= 21 && day <= 21)
            return new SeasonalTheme("World Music Day","🎵",
                "music concert colorful lights stage","World Music Day! 🎶");
        if (month == 10 && day >= 16 && day <= 16)
            return new SeasonalTheme("World Food Day","🍽️",
                "food colorful fruits vegetables harvest","World Food Day 🍽️");
        if (month == 11 && day >= 11 && day <= 11)
            return new SeasonalTheme("Remembrance Day","🌺",
                "red poppy field sunrise peaceful memorial","Remembrance Day 🌺");

        // ══════════════════════════════════════════
        // SEASONS
        // ══════════════════════════════════════════
        if (month == 12 && day >= 21 && day <= 23)
            return new SeasonalTheme("Winter Solstice","❄️",
                "winter solstice snow ice crystal night","Winter Solstice ❄️");
        if (month == 6 && day >= 20 && day <= 22)
            return new SeasonalTheme("Summer Solstice","☀️",
                "summer solstice golden sun beach warm","Summer Solstice ☀️");
        if (month == 3 && day >= 19 && day <= 21)
            return new SeasonalTheme("Spring Equinox","🌸",
                "spring flowers bloom colorful garden","Spring Equinox 🌸");
        if (month == 9 && day >= 22 && day <= 24)
            return new SeasonalTheme("Autumn Equinox","🍂",
                "autumn fall leaves golden orange forest","Autumn Equinox 🍂");

        return null;
    }
    public static boolean isActive() { return getActiveTheme() != null; }
}
