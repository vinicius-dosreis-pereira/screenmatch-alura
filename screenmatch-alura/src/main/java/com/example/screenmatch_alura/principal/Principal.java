package com.example.screenmatch_alura.principal;

import com.example.screenmatch_alura.model.DadosSerie;
import com.example.screenmatch_alura.model.DadosTemporada;
import com.example.screenmatch_alura.service.ConsumoApi;
import com.example.screenmatch_alura.service.ConverteDados;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=7df3ca37";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para a busca");
        String nomeSerie = leitura.nextLine();
        String json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> dadosTemporadaList = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            dadosTemporadaList.add(dadosTemporada);
        }
        dadosTemporadaList.forEach(System.out::println);

        dadosTemporadaList.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
//      utilizando função Lambda para substituir um 'for'

    }

}
