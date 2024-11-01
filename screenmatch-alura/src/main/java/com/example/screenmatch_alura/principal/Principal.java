package com.example.screenmatch_alura.principal;

import com.example.screenmatch_alura.model.DadosEpisodio;
import com.example.screenmatch_alura.model.DadosSerie;
import com.example.screenmatch_alura.model.DadosTemporada;
import com.example.screenmatch_alura.model.Episodio;
import com.example.screenmatch_alura.service.ConsumoApi;
import com.example.screenmatch_alura.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=sua-chave";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para a busca");
        String nomeSerie = leitura.nextLine();
        String json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
//      utilizando função Lambda para substituir um 'for'

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());
//      utilizando .collect(Collectors.toList()) em vez de .toList(), pois o toList() gera uma lista imutável

        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numeroTemporada(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("Digite uma parte do nome de um episodio que você gostaria de pesquisar");
        var nomeBusca = leitura.nextLine();

        Optional<Episodio> buscaEpisodio = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(nomeBusca.toUpperCase()))
                .findFirst();

        if(buscaEpisodio.isPresent()){
            System.out.println("\nEpisódio encontrado!");
            System.out.println("\nTemporada: " + buscaEpisodio.get().getTemporada());
            System.out.println("Título: " + buscaEpisodio.get().getTitulo());
            System.out.println("Avaliação: " + buscaEpisodio.get().getAvaliacao());
        } else {
            System.out.println("Episódio não encontrado");
        }

        System.out.println("\nA partir de qual ano você deseja ver os episodios?");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                                "\nTemporada: " + e.getTemporada() +
                                "\nEpisodio: " + e.getTitulo() +
                                "\nData de lançamento: " + e.getDataLancamento().format(formatter)
                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("\nMédia: " + est.getAverage());
        System.out.println("Melhor nota: " + est.getMax());
        System.out.println("Pior nota: " + est.getMin());
        System.out.println("Quantidade de episódios: " + est.getCount());


    }

}
