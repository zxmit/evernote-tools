package com.zxm.everynote.tools;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zxm on 13/02/2017.
 */
public class StickiesPersistent {

    // your evernote developer token
    private static String TOKEN = "***";
    private static String GUID = "67266787-36af-4a7a-8d06-2c1f52435386";
    private UserStoreClient userStore;
    private NoteStoreClient noteStore;

    public StickiesPersistent() throws Exception {
        init();
    }

    //初始化UserStore和NoteStore客户端
    public void init() throws Exception {
        //设置UserStore的客户端并且检查和服务器的连接
        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.YINXIANG, TOKEN);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        userStore = factory.createUserStoreClient();
        noteStore = factory.createNoteStoreClient();
    }

    private void listNotes() throws Exception {
        // 获取一个笔记本的列表
        List<Notebook> notebooks = noteStore.listNotebooks();

        for (Notebook notebook : notebooks) {
            System.out.println("Notebook: " + notebook.getName());
            // 搜索笔记本中前100个笔记并按创建日期排序，创建一个过滤器
            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(notebook.getGuid());
            filter.setOrder(NoteSortOrder.CREATED.getValue());
            filter.setAscending(true);

            NoteList noteList = noteStore.findNotes(filter, 0, 100);
            List<Note> notes = noteList.getNotes();
            for (Note note : notes) {
                System.out.println(" * " + note.getTitle());
            }
        }
    }

    public Note makeNote(String noteTitle, String noteBody, Notebook parentNotebook) {

        String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        nBody += "<en-note>" + noteBody + "</en-note>";
        // Create note object
        Note ourNote = new Note();
        ourNote.setTitle(noteTitle.trim());
        ourNote.setContent(nBody);

        // parentNotebook is optional; if omitted, default notebook is used
        if (parentNotebook != null && parentNotebook.isSetGuid()) {
            ourNote.setNotebookGuid(parentNotebook.getGuid());
        }

        Note note = null;
        try {
            note = noteStore.createNote(ourNote);
        } catch (EDAMUserException edue) {
            System.out.println("EDAMUserException: " + edue + " title: " + noteTitle);
        } catch (EDAMNotFoundException ednfe) {
            System.out.println("EDAMNotFoundException: Invalid parent notebook GUID");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return created note object
        return note;

    }

    public Map<String, Object> readNote(String path) {
        Map<String, Object> result = new HashMap<String, Object>();
        FileInputStream fin = null;
        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            fin = new FileInputStream(path);
            in = new InputStreamReader(fin, "UTF-16");
            br = new BufferedReader(in);
            String line = null;
            String title = "";
            List<String> lines = new ArrayList<String>();

            if((line = br.readLine()) != null) {
                title = line;
            } else {
                return null;
            }
            while((line = br.readLine()) != null) {
                lines.add(line);
            }
            result.put("title", title);
            result.put("lines", lines);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean writeNote(Map<String, Object> note) {
        Notebook notebook = new Notebook();
        notebook.setGuid(GUID);
        List<String> content = (List<String>)note.get("lines");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String title = sdf.format(new Date())+"."+note.get("title");
        String body = addStyle(content);
        Note node = makeNote(title, body, notebook);
        if(node == null) {
            return false;
        }
        return true;
    }

    public String addStyle(List<String> content) {
        String body = "";
        for(String str : content) {
            body += "<div style=\"font-weight:bold;color:blue;\"><![CDATA["+str+"]]></div>";
        }
        return body;
    }

    public String getNoteBookGuid(NoteStoreClient noteStore, String bookName) {
        List<Notebook> notebooks = null;
        try {
            notebooks = noteStore.listNotebooks();
            for (Notebook notebook : notebooks) {
                if("每日计划".equals(notebook.getName())) {
                    return notebook.getGuid();
                }
            }
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws Exception {

        if(args.length != 1) {
            System.exit(0);
        }
        StickiesPersistent hello = new StickiesPersistent();
        String str = args[0];
        String[] paths = str.split(",");
        for(String path : paths) {
            Map<String, Object> result = hello.readNote(path);
            if(hello.writeNote(result)) {
                File file = new File(path);
                file.delete();
            }
        }
    }

}
