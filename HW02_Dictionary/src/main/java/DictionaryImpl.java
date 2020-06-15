public class DictionaryImpl implements Dictionary {
    private int capacity = 10;

    private final double maxLoadFactor = 0.75;
    private final double minLoadFactor = 0.1;

    private int size;

    private String[] keys;
    private String[] values;

    public DictionaryImpl() {
        keys = new String[capacity];
        values = new String[capacity];
    }

    private int getBucketNumber(String key) {
        if (key.hashCode() < 0) {
            return -key.hashCode() % capacity;
        } else {
            return key.hashCode() % capacity;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    private void rehash(boolean increase) {
        int oldCapacity = capacity;
        if (increase) {
            capacity *= 2;
        } else {
            capacity /= 2;
        }
        String[] newKeys = new String[capacity];
        String[] newValues = new String[capacity];
        for (int i = 0; i < oldCapacity; i++) {
            if (keys[i] != null) {
                int index = getBucketNumber(keys[i]);
                newKeys[index] = keys[i];
                newValues[index] = values[i];
            }

        }
        keys = newKeys;
        values = newValues;
    }

    public int size() {
        return size;
    }

    // true, если такой ключ содержится в таблице
    public boolean contains(String key) {
        int bucket = getBucketNumber(key);
        if (keys[bucket] == key) {
            return true;
        } else {
            return false;
        }
    }

    // возвращает значение, хранимое по ключу key
    // если такого нет, возвращает null
    public String get(String key) {
        if (contains(key)) {
            int bucket = getBucketNumber(key);
            return values[bucket];
        } else {
            return null;
        }
    }

    // положить по ключу key значение value
    // и вернуть ранее хранимое, либо null;
    // провести рехеширование по необходимости
    public String put(String key, String value) {
        String result = null;
        int bucket = getBucketNumber(key);
        if (values[bucket] != null) {
            result = values[bucket];
        } else {
            size++;
        }
        keys[bucket] = key;
        values[bucket] = value;
        if ((double) size / capacity > maxLoadFactor) {
            rehash(true);
        }
        return result;
    }

    // забыть про пару key-value для переданного key
    // и вернуть забытое value, либо null, если такой пары не было;
    // провести рехеширование по необходимости
    public String remove(String key) {
        String oldValue = null;
        if (contains(key)) {
            int bucket = getBucketNumber(key);
            keys[bucket] = null;
            oldValue = values[bucket];
            values[bucket] = null;
            size--;
            if ((double) size / capacity < minLoadFactor) {
                rehash(false);
            }
        }
        return oldValue;
    }

    // забыть про все пары key-value
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            if (keys[i] != null) {
                int index = getBucketNumber(keys[i]);
                keys[index] = null;
                values[index] = null;
                size--;
            }
        }
    }
}

