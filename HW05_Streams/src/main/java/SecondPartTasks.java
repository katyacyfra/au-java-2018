import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllLines;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream().flatMap((String p) -> {
            try {
                return readAllLines(Paths.get(p)).stream();
            }
                catch (IOException e){
                    //System.out.println("Can't read all lines");
                    e.printStackTrace();
                }
                return Stream.empty();

            })
                .filter((String s) -> s.contains(sequence)).collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        Random random = new Random();
        long attempts = 1000;
        return random.doubles(attempts)
                .map(x -> Math.pow(x - 0.5, 2))
                .map(x -> Math.pow(random.nextDouble() - 0.5, 2) + x)
                .filter(s -> s <= Math.pow(0.5, 2)).count() / attempts;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .max(Comparator.comparing(e -> e.getValue()
                        .stream()
                        .collect(Collectors.summarizingInt(String::length))
                        .getSum()))
                .orElseThrow(RuntimeException::new).getKey();
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders
                .stream().flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
    }
}
