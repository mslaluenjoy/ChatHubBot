/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.factory;

import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class GlobalChatRoomFactory {

    public static void initiate() {
        if (GlobalChatRoom.loadAll() == null) {
            GlobalChatRoom hentai = new GlobalChatRoom(null, "هنتای (ゝ‿ മ)", "Hentai (ゝ‿ മ)", "به صفحه گلوبال هنتای خوش آمدید.", "Welcome to Hentai Global Room.");
            GlobalChatRoom relegion = new GlobalChatRoom(null, "دین 🛐", "Religion 🛐", "به صفحه دین خوش آمدید.", "Welcome to Relegion Global Room.");
            GlobalChatRoom university = new GlobalChatRoom(null, "دانشگاه 🧑‍🎓", "University 🧑‍🎓", "به صفحه دانشگاه خوش آمدید.", "Welcome to University Global Room.");
            GlobalChatRoom sexyBeautifulWomen = new GlobalChatRoom(null, "زن های سکسی زیبا 😍", "Sexy Beautiful Women 😍", "به صفحه گلوبال زن های سکسی زیبا خوش آمدید.", "Welcome to Sexy Beautiful Women Global Room.");
            GlobalChatRoom hardcore = new GlobalChatRoom(null, "هاردکور 🔨", "Hardcore 🔨", "به صفحه گلوبال هاردکور خوش آمدید.", "Welcome to Hardcore Global Room.");
            GlobalChatRoom politics = new GlobalChatRoom(null, "سیاست 🗳", "Politics 🗳", "به صفحه گلوبال سیاست خوش آمدید.", "Welcome to Politics Global Room.");
            GlobalChatRoom philosophy = new GlobalChatRoom(null, "فلسفه 🤔", "Philosophy 🤔", "به صفحه گلوبال فلسفه خوش آمدید.", "Welcome to Philosophy Global Room.");
            GlobalChatRoom music = new GlobalChatRoom(null, "موسیقی 🎸", "Music 🎸", "به صفحه گلوبال موسیقی خوش آمدید.", "Welcome to Music Global Room.");
            GlobalChatRoom art = new GlobalChatRoom(null, "هنر 🎭", "Art 🎭", "به صفحه گلوبال هنر خوش آمدید.", "Welcome to Art Global Room.");
            GlobalChatRoom japanese = new GlobalChatRoom(null, "فرهنگ ژاپنی 🇯🇵", "Japanese Culture 🇯🇵", "به صفحه گلوبال فرهنگ ژاپنی خوش آمدید.", "Welcome to Japanese Culture Global Room.");
            GlobalChatRoom videoGame = new GlobalChatRoom(null, "بازی ویدیویی 🎮", "Video Game 🎮", "به صفحه گلوبال بازی ویدیویی خوش آمدید.", "Welcome to Video Game Global Room.");
            GlobalChatRoom photography = new GlobalChatRoom(null, "عکاسی 📸", "Photography 📸", "به صفحه گلوبال عکاسی خوش آمدید.", "Welcome to Photography Global Room.");
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(hentai);
                    session.saveOrUpdate(sexyBeautifulWomen);
                    session.saveOrUpdate(hardcore);
                    session.saveOrUpdate(politics);
                    session.saveOrUpdate(philosophy);
                    session.saveOrUpdate(music);
                    session.saveOrUpdate(art);
                    session.saveOrUpdate(japanese);
                    session.saveOrUpdate(videoGame);
                    session.saveOrUpdate(photography);
                    session.saveOrUpdate(relegion);
                    session.saveOrUpdate(university);
                }
            }.execute();
        }
    }
}
