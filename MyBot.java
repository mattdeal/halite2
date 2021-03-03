import hlt.*;
import v2.bots.StrategyBot;

public class MyBot {
    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("BetterBot");

        StrategyBot bot = new StrategyBot(networking, gameMap);
        bot.run();
    }
}
