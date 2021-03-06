package pl.cafebabe.kebab.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import pl.cafebabe.kebab.mongodb.codec.BigDecimalSerializer;

public class Cena {

	/* wymagane przez Jongo do poprawnego zapisu liczby */
	@JsonSerialize(using = BigDecimalSerializer.class)
	private BigDecimal kwota;
	private Waluta waluta;

	public Cena() {
	}

	public Cena(BigDecimal kwota, Waluta waluta) {
		this.kwota = kwota;
		this.waluta = waluta;
	}

	public BigDecimal getKwota() {
		return kwota;
	}

	public void setKwota(BigDecimal kwota) {
		this.kwota = kwota;
	}

	public Waluta getWaluta() {
		return waluta;
	}

	public void setWaluta(Waluta waluta) {
		this.waluta = waluta;
	}

	@Override
	public String toString() {
		return String.format("%s %s", kwota, waluta);
	}

}