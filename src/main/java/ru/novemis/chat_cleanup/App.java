package ru.novemis.chat_cleanup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class App {

    private static final Integer MIN_COUNT = 50;
    private static int WEEK_COUNT = 1;
    private static int CONVERSATION_ID = 2000000001;

    public static void main(String[] args) throws Throwable {
        List<UserDto> userDtos = VkRequests.getConversationMembers(CONVERSATION_ID);
        VkRequests.send(CONVERSATION_ID, "Сейчас будем удалять пользователей, которые писали меньше " + MIN_COUNT + " сообщений за последнюю неделю. Мы вас предупреждали!");
        fillUserMsgCount(userDtos);
        List<UserDto> usersToRemove = userDtos.stream()
          .filter(it -> it.msgsCount < MIN_COUNT)
          .collect(Collectors.toList());

        if (!usersToRemove.isEmpty()) {
            String namesJoined = usersToRemove.stream().map(it -> it.firstName + " " + it.lastName).collect(Collectors.joining(", "));
            VkRequests.send(CONVERSATION_ID, "Удаляем: " + namesJoined);
            for (UserDto userDto : usersToRemove) {
                VkRequests.removeChatUser(CONVERSATION_ID - 2000000000, userDto.userId);
            }
        } else {
            VkRequests.send(CONVERSATION_ID, "Ложная тревога :) все активно общались, вы молодцы!");
        }

    }

    private static void fillUserMsgCount(List<UserDto> users) throws Throwable {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).minusWeeks(WEEK_COUNT);
        long weekAgoUnix = zonedDateTime.toEpochSecond();
        Long lastMsgId = null;

        /*
        Сделать первый запрос.
        далее запрашиваем пока не будет с датой < минимальной (это изнутри условие) или количество сообщений == 1.

        Запросить по какую то дату. Как: запрашиваю по 200. Сохраняю в результат. Если дата больше чем неделя - прерываю.
        Запрашиваю дальше до конца, до тех пор пока не придёт 1 сообщение в результате или не будет найдено сообщение с минимальным.


         */

        JSONObject history = VkRequests.getHistory(CONVERSATION_ID);
        JSONArray items = history.getJSONObject("response").getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            lastMsgId = item.getLong("id");
            long itemDate = item.getLong("date");
            int userId = item.getInt("from_id");
            Optional<UserDto> optional = users.stream().filter(it -> it.userId == userId).findAny();

            if (optional.isPresent()) {
                UserDto userDto = optional.get();
                userDto.msgsCount += 1;
                if (itemDate < weekAgoUnix) {
                    return;
                }
            }
        }

//        while (true) {
//            JSONObject nextHistory = VkRequests.getHistory(CONVERSATION_ID, lastMsgId);
//            JSONArray nextItems = nextHistory.getJSONObject("response").getJSONArray("items");
//
//            if (nextItems.length() == 1) {
//                return;
//            }
//
//            for (int i = 0; i < nextItems.length(); i++) {
//                JSONObject item = items.getJSONObject(i);
//                lastMsgId = item.getLong("id");
//                long itemDate = item.getLong("date");
//                int userId = item.getInt("from_id");
//                Optional<UserDto> optional = users.stream().filter(it -> it.userId == userId).findAny();
//                if (optional.isPresent()) {
//                    UserDto userDto = optional.get();
//                    userDto.msgsCount += 1;
//                    if (itemDate < weekAgoUnix) {
//                        return;
//                    }
//                }
//            }
//        }
    }
}
