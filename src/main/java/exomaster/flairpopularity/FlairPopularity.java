package exomaster.flairpopularity;

import exomaster.flairpopularity.config.Config;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.*;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import javax.print.DocFlavor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class FlairPopularity {
    private static FlairPopularity INSTANCE;

    public static FlairPopularity getInstance() {
        return INSTANCE;
    }

    private Config config;
    private RedditClient client;

    public static void main(String[] args) {
        new FlairPopularity();
    }

    private HashMap<String, Integer> popularityMap = new HashMap<>();
    private ArrayList<String> usersFound = new ArrayList<>();

    private void performListing(SubredditSort sort) {
        SubredditReference sub = client.subreddit(config.getEntry("SUBREDDIT", String.class));
        DefaultPaginator<Submission> pb = sub.posts()
                .sorting(sort)
                .timePeriod(TimePeriod.ALL)
                .build();

        Listing<Submission> currListings;
        int page = 0;

        try {
            while (pb.iterator().hasNext()) {
                page++;
                currListings = pb.next();
                consoleLog("Opening page " + page + ".");
                currListings.forEach(submission -> {
                    consoleLog("Reading post: \"" + submission.getTitle() + "\"." );
                    consoleLog(submission.getAuthor() + ", " + submission.getAuthorFlairText());

                    //Comment code. This requires moderator permission, but I'm leaving this for future reference, in case I decide to use this on a subreddit I have moderator permissions on.
//                    RootCommentNode root = client.submission(submission.getId()).comments();
//                    Iterator<CommentNode<PublicContribution<?>>> it = root.walkTree().iterator();
//
//                    while (it.hasNext()) {
//                        PublicContribution<?> thing = it.next().getSubject();
//
//                        if (!usersFound.contains(thing.getAuthor())) {
//                            if (sub.otherUserFlair(thing.getAuthor()).current().isPresent()) {
//                                String flair = sub.otherUserFlair(thing.getAuthor()).current().getText();
//                                consoleLog("Comment: " + thing.getAuthor() + ", " + flair);
//                                popularityMap.put(flair, popularityMap.getOrDefault(flair, 0) + 1);
//                            }
//
//                            usersFound.add(thing.getAuthor());
//                        }
//                    }

                    if (!usersFound.contains(submission.getAuthor())) {
                        if (submission.getAuthorFlairText() != null)
                            popularityMap.put(submission.getAuthorFlairText(), popularityMap.getOrDefault(submission.getAuthorFlairText(), 0) + 1);

                        usersFound.add(submission.getAuthor());
                    }
                });
                consoleLog("Closing page " + page + ".");
                Thread.sleep(1000);
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    public FlairPopularity() {
        INSTANCE = this;
        config = new Config("Config.txt");
        if (this.config.getHasUpdatedConfigFile()) {
            consoleLog("Config.txt has been created or updated with missing values. Please input the proper values of each new entry before running this bot again.");
            System.exit(1);
        }

        UserAgent userAgent = new UserAgent("bot", "exomaster.flairpopularity", "v1.0", "flairpopularity");
        Credentials credentials = Credentials.script(config.getEntry("REDDIT_USERNAME", String.class), config.getEntry("REDDIT_PASSWORD", String.class),
                config.getEntry("SCRIPT_CLIENTID", String.class), config.getEntry("SCRIPT_SECRET", String.class));

        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        client = OAuthHelper.automatic(adapter, credentials);

        consoleLog("Sorting by new.");
        performListing(SubredditSort.NEW);
        consoleLog("Sorting by top.");
        performListing(SubredditSort.TOP);

        try {
            int numFlairs = 0;

            PrintWriter w = new PrintWriter("output.txt", "UTF-8");
            for (Map.Entry<String, Integer> entry : popularityMap.entrySet()) {
                numFlairs+=entry.getValue();
                w.println(entry.getKey() + ": " + entry.getValue());
            }
            w.close();

            consoleLog("Popularity listing complete. Polled " + usersFound.size() + " unique users, of which " + numFlairs + " had flairs. Press enter to close.");
            System.in.read();
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void consoleLog(Object... params) {
        StringBuilder sb = new StringBuilder();
        for (Object o : params)
            sb.append(o).append(' ');
        System.out.println('(' + new SimpleDateFormat("MM/dd/yy HH:mm:ss z").format(new Date()) + ") Flair-Popularity: " + sb.toString().trim());
    }

    public static Config getConfig() {
        return INSTANCE.config;
    }

    public static RedditClient getClient() {
        return INSTANCE.client;
    }
}
