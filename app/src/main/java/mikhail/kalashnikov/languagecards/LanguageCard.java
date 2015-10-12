    package mikhail.kalashnikov.languagecards;

    import android.provider.BaseColumns;

    import java.util.Comparator;

    public class LanguageCard implements BaseColumns {
        public static final String TABLE_NAME = "languagecard";
        public static final String COLUMN_WORD_LANG1 = "word_lang1";
        public static final String COLUMN_WORD_LANG2 = "word_lang2";
        public static final String COLUMN_GROUP_ID = "group_id";
        public static final String COLUMN_LEARNED = "learned";
        public static final String COLUMN_LESSON = "lesson";

        private long id;
        private String word_lang1;
        private String word_lang2;
        private long group_id;
        private int learned;
        private String lesson;

        public LanguageCard(long id, String word_lang1, String word_lang2, long group_id, int learned,
                            String lesson) {
            super();
            this.id = id;
            this.word_lang1 = word_lang1;
            this.word_lang2 = word_lang2;
            this.group_id = group_id;
            this.learned = learned;
            this.lesson = lesson;
        }

        public LanguageCard(String word_lang1, String word_lang2, long group_id, int learned,
                            String lesson) {
            super();
            this.word_lang1 = word_lang1;
            this.word_lang2 = word_lang2;
            this.group_id = group_id;
            this.learned = learned;
            this.lesson = lesson;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public String getWord_lang1() {
            return word_lang1;
        }

        public String getWord_lang2() {
            return word_lang2;
        }

        public long getGroup_id() {
            return group_id;
        }

        public void setWord_lang1(String word_lang1) {
            this.word_lang1 = word_lang1;
        }

        public void setWord_lang2(String word_lang2) {
            this.word_lang2 = word_lang2;
        }

        public boolean getLearned() {
            return learned != 0;
        }

        public String getLesson() {
            return lesson;
        }

        public void setLesson(String lesson) {
            this.lesson = lesson;
        }

        public void setLearned(int learned) {
            this.learned = learned;
        }

        @Override
        public String toString() {
            return "LanguageCard{" +
                    "id=" + id +
                    ", word_lang1='" + word_lang1 + '\'' +
                    ", word_lang2='" + word_lang2 + '\'' +
                    ", group_id=" + group_id +
                    ", learned=" + learned +
                    ", leeson=" + lesson +
                    '}';
        }

        public static Comparator<LanguageCard> getWord1Comparator() {
            return new Comparator<LanguageCard>() {
                @Override
                public int compare(LanguageCard lhs, LanguageCard rhs) {
                    return lhs.getWord_lang1().compareTo(rhs.getWord_lang1());
                }
            };
        }

        public static Comparator<LanguageCard> getWord2Comparator() {
            return new Comparator<LanguageCard>() {
                @Override
                public int compare(LanguageCard lhs, LanguageCard rhs) {
                    return lhs.getWord_lang2().compareTo(rhs.getWord_lang2());
                }
            };
        }
    }
