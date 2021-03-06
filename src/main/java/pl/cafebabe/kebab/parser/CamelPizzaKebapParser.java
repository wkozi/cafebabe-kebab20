package pl.cafebabe.kebab.parser;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pl.cafebabe.kebab.IMenuProvider;
import pl.cafebabe.kebab.model.Cena;
import pl.cafebabe.kebab.model.Grupa;
import pl.cafebabe.kebab.model.Menu;
import pl.cafebabe.kebab.model.Pozycja;
import pl.cafebabe.kebab.model.Restauracja;
import pl.cafebabe.kebab.model.Waluta;
import pl.cafebabe.kebab.model.Wariant;

//TODO trzeba zrobic wersjonowanie api v1, v2 itd.
//TODO REST np. /menu/{restauracja}, /menu/{restaurcja}/{grupa} "/menu/camel/kebab"
public class CamelPizzaKebapParser implements IMenuProvider {

	public static void parse() throws Exception {
		File input = new File("c:/temp/KEBAP _ CamelPizza - Bydgoszcz.html");
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		// <div class="post-entry">
		Elements e = doc.select("div.post-entry").select("tr");
		// e.stream().forEach(i -> System.out.println(i.toString()));

		for (Element i : e) {
			Elements td = i.select("td");
			if (td.size() >= 2) {
				String nazwa = td.get(0).text();
				String cena = td.get(1).text();

				if (StringUtils.isNoneBlank(nazwa, cena)) {
					System.out.printf("nazwa: %s, cena: %s\n", nazwa, cena);
				}
			}
		}

	}

	@Override
	public Menu getMenu() throws Exception {
		Menu menu = new Menu();
		menu.setAktualnosc(new Date());

		Restauracja restauracja = new Restauracja();
		restauracja.setNazwa("CamelPizza");
		restauracja.setLogo("http://camelpizza.pl/wp-content/uploads/2014/06/logo.png");
		restauracja.setUrl("http://camelpizza.pl");
		menu.setRestauracja(restauracja);

		String[][] grupy = {
			{ "Pizza", "http://camelpizza.pl/pizza" },
			{ "Kebab", "http://camelpizza.pl/kebap" },
			{ "Obiady", "http://camelpizza.pl/obiady" },
			{ "Spaghetti", "http://camelpizza.pl/spaghetti" },
			{ "Burger", "http://camelpizza.pl/burgery" },
			{ "Sałatki", "http://camelpizza.pl/salatki" },
			{ "Dla malucha", "http://camelpizza.pl/dla-malucha"},
		};
		for (String[] i : grupy) {
			menu.getGrupy().add(getGrupa(i[0], i[1]));
		}
		return menu;
	}

	private Cena parseCena(String x) {
		try {
			Cena cena = new Cena();			
			String kwota = x.replaceAll("zł", "").replace((char) 160, ' ') .trim().replace(",", ".");
			cena.setKwota(new BigDecimal(kwota).setScale(2, BigDecimal.ROUND_HALF_UP));
			cena.setWaluta(Waluta.PLN);
			// TODO enum z walutami, a może JSR 354 - Currency and Money
			return cena;
		} catch (Exception ex) {
			// TODO usunąć do metody
//			for (int i = 0; i < x.length(); i++) {
//				System.out.println(x.charAt(i) + "-" + (int) x.charAt(i));
//			}
			throw new RuntimeException(String.format("Nie można sparsować ceny (%s).", x));
		}
	}

	private Grupa getGrupa(String nazwa, String url) throws Exception {
		Grupa grupa = new Grupa();
		grupa.setNazwa(nazwa);
		System.out.println(grupa.getNazwa());
//		File input = new File("c:/temp/KEBAP _ CamelPizza - Bydgoszcz.html");
//		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		//Document doc = Jsoup.connect("http://camelpizza.pl/kebap").get();
		Connection connection = Jsoup.connect(url);
		connection.userAgent("Mozilla/5.0");
		Document doc = connection.get();
		
		Elements e = doc.select("div.post-entry").select("tr");
		// e.stream().forEach(i -> System.out.println(i.toString()));

		List<String> warianty = new ArrayList<>();
		for (int i = 0; i < e.size(); i++) {
			Elements td = e.get(i).select("td");
			final int c = td.size();

			if (c >= 2) {
				// warianty mogą występować jako wyszczególnione lub od razu może być pozycja
				boolean w = false;
				for (int j = 1; j < c; j++) {
					String t = td.get(j).text().trim();
					if (t.endsWith("zł")) {
						warianty.add(StringUtils.EMPTY);
					} else {
						warianty.add(t);
						w = true;
					}
				}

				if (w) {
					continue;
				}

				String nazwaPozycji = td.get(0).text().trim();
				if (StringUtils.isBlank(nazwaPozycji)) {
					continue;
				}
				
				Pozycja pozycja = new Pozycja();
				//TODO parsowanie opisu
//				Pattern pattern = Pattern.compile("(?<nazwa>\\d+\\.(.*)) - (?<opis>.*)");
//				Matcher matcher = pattern.matcher(nazwaPozycji);
//				if (matcher.matches()) {
//					pozycja.setNazwa(matcher.group("nazwa"));
//					pozycja.setOpis(matcher.group("opis"));
//				} else {
//					pozycja.setNazwa(nazwaPozycji);
//					pozycja.setOpis(StringUtils.EMPTY);
//				}
				pozycja.setNazwa(nazwaPozycji);
				pozycja.setOpis(StringUtils.EMPTY);
				
				System.out.println(pozycja.getNazwa());
				for (int j = 1; j < c; j++) {
					String nazwaWariantu = warianty.get(j - 1);
					String[] luby = nazwaWariantu.split(" lub ");
					for (String lub : luby) {
						Wariant wariant = new Wariant();
						wariant.setOpis(lub.trim());
						wariant.getCeny().add(parseCena(td.get(j).text()));
						pozycja.getWarianty().add(wariant);
					}
				}
				grupa.getPozycje().add(pozycja);
			}
			
		}
	
		return grupa;
	}

	public static void main(String[] args) throws Exception {
		//parse();
		CamelPizzaKebapParser parser = new CamelPizzaKebapParser();
		Menu menu = parser.getMenu();
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(menu);
		System.out.println(json);
	}

}
