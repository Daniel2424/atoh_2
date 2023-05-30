package pr2;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Scene {
    private static final BlockingQueue<Phrase> queue = new ArrayBlockingQueue<>(15);
    private static final Object lock = new Object();

    private static volatile boolean check = true;
    private static final Scanner scanner = new Scanner(System.in);
    private static final Set<String> setActors = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Enter the number of characters in the skit\n");
        int countActor = scanner.nextInt();
        scanner.nextLine();
        String[] actors = new String[countActor];
        for (int i = 0; i < countActor; i++) {
            System.out.println("Enter the actor's name");
            actors[i] = scanner.nextLine();
            setActors.add(actors[i].toLowerCase());
        }
        //Recording phrases
        new Thread(Scene::produce).start();

        //Sitcom actors
        for (String actor : actors) {
            new Thread(() -> consumer(actor)).start();
        }

    }

    private static void produce() {
        System.out.println("Enter name and phrase with \":\", " +
                "for example \"Daniil: Hello!\" " +
                "or enter \"end\" to finish");
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String replica = scanner.nextLine();
            if(replica.equalsIgnoreCase("end")){
                check = false;
                break;
            }
            Phrase phrase = new Phrase(replica);
            try {
                queue.put(phrase);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        scanner.close();

    }
    private static void consumer(String actorName){

        while(true){
            synchronized (lock){

                if(!check && queue.size() == 0) break;
                Phrase phrase = queue.peek();
                if(phrase!= null && !setActors.contains(phrase.getName().toLowerCase())){
                    try {
                        queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("There is no actor for this replic");
                }
                if(phrase != null && phrase.getName().equalsIgnoreCase(actorName)){
                    try {
                        System.out.println(actorName + ": " + queue.take().getPhrase());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

        }
    }
}

class Phrase{
    private String name;
    private String phrase;

    public Phrase(String replica) {
        String[] name_phrase = replica.split(":");
        if(name_phrase.length ==1){
            this.name = "None";
            this.phrase = name_phrase[0];
        }else{
            this.name = name_phrase[0];
            this.phrase = name_phrase[1];

        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }
}
