/* Useful info about git internals
https://maryrosecook.com/blog/post/git-in-six-hundred-words
https://maryrosecook.com/blog/post/git-from-the-inside-out
https://marklodato.github.io/visual-git-guide/index-ru.html#checkout
https://git-scm.com/book/ru/v2/Git-%D0%B8%D0%B7%D0%BD%D1%83%D1%82%D1%80%D0%B8-%D0%9E%D0%B1%D1%8A%D0%B5%D0%BA%D1%82%D1%8B-Git
https://picocli.info/
https://medium.com/@gurayy/how-git-really-works-part-1-how-git-add-works-under-the-hood-2c6221c48b91
*/


import picocli.CommandLine;


public class Main {

    public static void main(String[] args) {
        CommandLine.run(new Git(), System.err, args);

    }


}
