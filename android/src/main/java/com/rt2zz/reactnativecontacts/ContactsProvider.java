package com.rt2zz.reactnativecontacts;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.CommonDataKinds.Nickname;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import static android.provider.ContactsContract.CommonDataKinds.Im;
import static android.provider.ContactsContract.CommonDataKinds.Organization;
import static android.provider.ContactsContract.CommonDataKinds.Relation;
import static android.provider.ContactsContract.CommonDataKinds.Event;
// import static android.provider.ContactsContract.CommonDataKinds.Photo;
import static android.provider.ContactsContract.CommonDataKinds.Note;
// import static android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import static android.provider.ContactsContract.CommonDataKinds.Website;
import static android.provider.ContactsContract.CommonDataKinds.SipAddress;
import static android.provider.ContactsContract.CommonDataKinds.Identity;


public class ContactsProvider {
    public static final int ID_FOR_PROFILE_CONTACT = -1;

    private static final List<String> JUST_ME_PROJECTION = new ArrayList<String>() {{
        // DataColumns
        add(Data.MIMETYPE);
        // add(Data.RAW_CONTACT_ID); Not Needed
        add(Data.IS_PRIMARY);
        // add(Data.IS_SUPER_PRIMARY); Not Needed

        // RawContactsColumns
        add(Data.CONTACT_ID);
        // add(Data.DELETED); Invalid Column
        add(Data.RAW_CONTACT_IS_USER_PROFILE);

        // SyncColumns
        add(RawContacts.ACCOUNT_NAME);
        add(RawContacts.ACCOUNT_TYPE);
        add(RawContacts.SOURCE_ID);
        // add(RawContacts.VERSION);

        // ContactsColumns
        add(Contacts.DISPLAY_NAME);
        add(Contacts.PHOTO_URI);
        add(Contacts.PHOTO_THUMBNAIL_URI);
        // add(Contacts.IS_USER_PROFILE); Invalid Column
        add(Contacts.LOOKUP_KEY);
        if (Build.VERSION.SDK_INT >= 18) {
            add(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
        }

        // ContactOptionsColumns
        add(Contacts.TIMES_CONTACTED);
        add(Contacts.LAST_TIME_CONTACTED);
        add(Contacts.STARRED);
        // add(Contacts.PINNED);

        add(StructuredName.DISPLAY_NAME);
        add(StructuredName.GIVEN_NAME);
        add(StructuredName.FAMILY_NAME);
        add(StructuredName.PREFIX);
        add(StructuredName.MIDDLE_NAME);
        add(StructuredName.SUFFIX);
        add(StructuredName.PHONETIC_GIVEN_NAME);
        add(StructuredName.PHONETIC_MIDDLE_NAME);
        add(StructuredName.PHONETIC_FAMILY_NAME);
        // if (Build.VERSION.SDK_INT >= 21) {
        //     add(StructuredName.FULL_NAME_STYLE);
        // }
        // add(StructuredName.PHONETIC_NAME_STYLE);

        add(Nickname.TYPE);
        add(Nickname.LABEL);
        add(Nickname.NAME);

        add(Phone.TYPE);
        add(Phone.LABEL);
        add(Phone.NUMBER);
        add(Phone.NORMALIZED_NUMBER);

        add(Email.TYPE);
        add(Email.LABEL);
        add(Email.ADDRESS);
        add(Email.DISPLAY_NAME);

        add(StructuredPostal.TYPE);
        add(StructuredPostal.LABEL);
        add(StructuredPostal.FORMATTED_ADDRESS);
        add(StructuredPostal.STREET);
        add(StructuredPostal.POBOX);
        add(StructuredPostal.NEIGHBORHOOD);
        add(StructuredPostal.CITY);
        add(StructuredPostal.REGION);
        add(StructuredPostal.POSTCODE);
        add(StructuredPostal.COUNTRY);

        add(Im.TYPE);
        add(Im.LABEL);
        add(Im.PROTOCOL);
        add(Im.CUSTOM_PROTOCOL);

        add(Organization.TYPE);
        add(Organization.LABEL);
        add(Organization.COMPANY);
        add(Organization.TITLE);
        add(Organization.DEPARTMENT);
        add(Organization.JOB_DESCRIPTION);
        add(Organization.SYMBOL);
        add(Organization.PHONETIC_NAME);
        add(Organization.OFFICE_LOCATION);
        // add(Organization.PHONETIC_NAME_STYLE);

        add(Relation.TYPE);
        add(Relation.LABEL);
        add(Relation.NAME);

        add(Event.TYPE);
        add(Event.LABEL);
        add(Event.START_DATE);

        add(Note.NOTE);

        add(Website.TYPE);
        add(Website.LABEL);
        add(Website.URL);

        add(SipAddress.TYPE);
        add(SipAddress.LABEL);
        add(SipAddress.SIP_ADDRESS);

        add(Identity.IDENTITY);
        add(Identity.NAMESPACE);
    }};

    private static final List<String> FULL_PROJECTION = new ArrayList<String>() {{
        addAll(JUST_ME_PROJECTION);
    }};

    private static final List<String> PHOTO_PROJECTION = new ArrayList<String>() {{
        add(Contacts.PHOTO_URI);
    }};

    private final ContentResolver contentResolver;

    public ContactsProvider(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public WritableArray getContacts() {
        Map<String, Contact> justMe;
        {
            Cursor cursor = contentResolver.query(
                    Uri.withAppendedPath(
                            ContactsContract.Profile.CONTENT_URI,
                            Contacts.Data.CONTENT_DIRECTORY
                    ),
                    JUST_ME_PROJECTION.toArray(new String[JUST_ME_PROJECTION.size()]),
                    null,
                    null,
                    null
            );

            try {
                justMe = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        Map<String, Contact> everyoneElse;
        {
            Cursor cursor = contentResolver.query(
                    Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=? OR " +
                            Data.MIMETYPE + "=?",
                    new String[] {
                            StructuredName.CONTENT_ITEM_TYPE,
                            Nickname.CONTENT_ITEM_TYPE,
                            Phone.CONTENT_ITEM_TYPE,
                            Email.CONTENT_ITEM_TYPE,
                            StructuredPostal.CONTENT_ITEM_TYPE,
                            Im.CONTENT_ITEM_TYPE,
                            Organization.CONTENT_ITEM_TYPE,
                            Relation.CONTENT_ITEM_TYPE,
                            Event.CONTENT_ITEM_TYPE,
                            Note.CONTENT_ITEM_TYPE,
                            Website.CONTENT_ITEM_TYPE,
                            SipAddress.CONTENT_ITEM_TYPE,
                            Identity.CONTENT_ITEM_TYPE
                    },
                    null
            );

            try {
                everyoneElse = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : justMe.values()) {
            contacts.pushMap(contact.toMap());
        }
        for (Contact contact : everyoneElse.values()) {
            contacts.pushMap(contact.toMap());
        }

        return contacts;
    }

    private static String getString(Cursor cursor, String columnName) {
        String str = cursor.getString(cursor.getColumnIndex(columnName));
        if (str == null) return null;
        str = str.trim();
        return str.isEmpty() ? null : str;
    }

    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private static boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }

    @NonNull
    private Map<String, Contact> loadContactsFrom(Cursor cursor) {
        Map<String, Contact> map = new LinkedHashMap<>();

        while (cursor != null && cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(Data.CONTACT_ID);
            String contactId;
            if (columnIndex != -1) {
                contactId = cursor.getString(columnIndex);
            } else {
                //todo - double check this, it may not be necessary any more
                contactId = String.valueOf(ID_FOR_PROFILE_CONTACT); //no contact id for 'ME' user
            }

            Contact contact;
            if (map.containsKey(contactId)) {
                contact = map.get(contactId);
            } else {
                contact = new Contact(contactId);
                map.put(contactId, contact);

                contact.isUserProfile = getInt(cursor, Data.RAW_CONTACT_IS_USER_PROFILE) == 1;
                contact.accountName = getString(cursor, RawContacts.ACCOUNT_NAME);
                contact.accountType = getString(cursor, RawContacts.ACCOUNT_TYPE);
                contact.sourceId = getString(cursor, RawContacts.SOURCE_ID);

                contact.displayName = getString(cursor, Contacts.DISPLAY_NAME);
                contact.photoUri = getString(cursor, Contacts.PHOTO_URI);
                contact.photoThumbnailUri = getString(cursor, Contacts.PHOTO_THUMBNAIL_URI);
                contact.lookupKey = getString(cursor, Contacts.LOOKUP_KEY);
                if (Build.VERSION.SDK_INT >= 18) {
                    contact.lastUpdated = getString(cursor, Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
                }

                contact.timesContacted = getInt(cursor, Data.TIMES_CONTACTED);
                contact.lastContacted = getString(cursor, Data.LAST_TIME_CONTACTED);
                contact.starred = getInt(cursor, Data.STARRED) == 1;
            }

            switch (getString(cursor, Data.MIMETYPE)) {
                case StructuredName.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.names, new Contact.StructuredNameItem(cursor));
                    break;
                case Nickname.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.nicknames, new Contact.NicknameItem(cursor));
                    break;
                case Phone.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.phones, new Contact.PhoneItem(cursor));
                    break;
                case Email.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.emails, new Contact.EmailItem(cursor));
                    break;
                case StructuredPostal.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.postals, new Contact.StructuredPostalItem(cursor));
                    break;
                case Im.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.ims, new Contact.ImItem(cursor));
                    break;
                case Organization.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.organizations, new Contact.OrganizationItem(cursor));
                    break;
                case Relation.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.relations, new Contact.RelationItem(cursor));
                    break;
                case Event.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.events, new Contact.EventItem(cursor));
                    break;
                case Note.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.notes, new Contact.NoteItem(cursor));
                    break;
                case Website.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.websites, new Contact.WebsiteItem(cursor));
                    break;
                case SipAddress.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.sipAddresses, new Contact.SipAddressItem(cursor));
                    break;
                case Identity.CONTENT_ITEM_TYPE:
                    Contact.addIfValid(contact.identities, new Contact.IdentityItem(cursor));
                    break;
                default:
                    break;
            }
        }

        return map;
    }

    public String getPhotoUriFromContactId(String contactId) {
        Cursor cursor = contentResolver.query(
                Data.CONTENT_URI,
                PHOTO_PROJECTION.toArray(new String[PHOTO_PROJECTION.size()]),
                RawContacts.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        try {
            if (cursor != null && cursor.moveToNext()) {
                String rawPhotoURI = getString(cursor, Contacts.PHOTO_URI);
                if (!isEmptyString(rawPhotoURI)) {
                    return rawPhotoURI;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static class Contact {
        private String contactId;

        private boolean isUserProfile = false;
        private String accountName;
        private String accountType;
        private String sourceId;

        private String displayName;
        private String photoUri;
        private String photoThumbnailUri;
        private String lookupKey;
        private String lastUpdated;

        private int timesContacted = 0;
        private String lastContacted;
        private boolean starred = false;

        private List<StructuredNameItem> names = new ArrayList<>();

        private List<NicknameItem> nicknames = new ArrayList<>();

        private List<PhoneItem> phones = new ArrayList<>();

        private List<EmailItem> emails = new ArrayList<>();

        private List<StructuredPostalItem> postals = new ArrayList<>();

        private List<ImItem> ims = new ArrayList<>();

        private List<OrganizationItem> organizations = new ArrayList<>();

        private List<RelationItem> relations = new ArrayList<>();

        private List<EventItem> events = new ArrayList<>();

        private List<NoteItem> notes = new ArrayList<>();

        private List<WebsiteItem> websites = new ArrayList<>();

        private List<SipAddressItem> sipAddresses = new ArrayList<>();

        private List<IdentityItem> identities = new ArrayList<>();

        public Contact(String contactId) {
            this.contactId = contactId;
        }

        private static void putString(WritableMap map, String key, String value) {
            if (!isEmptyString(value)) {
                map.putString(key, value);
            }
        }

        private static <T extends BaseInfoItem> void addIfValid(List<T> list, T item) {
            if (item.isValid()) {
                if (item.isPrimary) {
                    list.add(0, item);
                } else {
                    list.add(item);
                }
            }
        }

        private static void putInfoArray(WritableMap map, String key, List<? extends BaseInfoItem> list) {
            if (!list.isEmpty()) {
                WritableArray array = Arguments.createArray();
                for (BaseInfoItem item : list) {
                    array.pushMap(item.toMap());
                }
                map.putArray(key, array);
            }
        }

        public WritableMap toMap() {
            WritableMap contact = Arguments.createMap();
            putString(contact, "contactId", contactId);

            contact.putBoolean("isUserProfile", isUserProfile);
            putString(contact, "accountName", accountName);
            putString(contact, "accountType", accountType);
            putString(contact, "sourceId", sourceId);

            putString(contact, "displayName", displayName);
            putString(contact, "photoUri", photoUri);
            putString(contact, "photoThumbnailUri", photoThumbnailUri);
            putString(contact, "lookupKey", lookupKey);
            putString(contact, "lastUpdated", lastUpdated);

            contact.putInt("timesContacted", timesContacted);
            putString(contact, "lastContacted", lastContacted);
            contact.putBoolean("starred", starred);

            putInfoArray(contact, "names", names);

            putInfoArray(contact, "nicknames", nicknames);

            putInfoArray(contact, "phones", phones);

            putInfoArray(contact, "emails", emails);

            putInfoArray(contact, "postals", postals);

            putInfoArray(contact, "ims", ims);

            putInfoArray(contact, "organizations", organizations);

            putInfoArray(contact, "relations", relations);

            putInfoArray(contact, "events", events);

            putInfoArray(contact, "notes", notes);

            putInfoArray(contact, "websites", websites);

            putInfoArray(contact, "sipAddresses", sipAddresses);

            putInfoArray(contact, "identities", identities);

            return contact;
        }

        abstract public static class BaseInfoItem {
            boolean isPrimary = false;

            BaseInfoItem(Cursor cursor) {
                isPrimary = getInt(cursor, Data.IS_PRIMARY) == 1;
            }

            abstract boolean isValid();

            WritableMap toMap() {
                WritableMap map = Arguments.createMap();
                map.putBoolean("isPrimary", isPrimary);
                return map;
            }
        }

        public static class StructuredNameItem extends BaseInfoItem {
            private String displayName;
            private String givenName;
            private String familyName;
            private String prefix;
            private String middleName;
            private String suffix;
            private String phoneticGivenName;
            private String phoneticMiddleName;
            private String phoneticFamilyName;
            private String fullNameStyle;
            private String phoneticNameStyle;

            public StructuredNameItem(Cursor cursor) {
                super(cursor);
                displayName = getString(cursor, StructuredName.DISPLAY_NAME);
                givenName = getString(cursor, StructuredName.GIVEN_NAME);
                familyName = getString(cursor, StructuredName.FAMILY_NAME);
                prefix = getString(cursor, StructuredName.PREFIX);
                middleName = getString(cursor, StructuredName.MIDDLE_NAME);
                suffix = getString(cursor, StructuredName.SUFFIX);
                phoneticGivenName = getString(cursor, StructuredName.PHONETIC_GIVEN_NAME);
                phoneticMiddleName = getString(cursor, StructuredName.PHONETIC_MIDDLE_NAME);
                phoneticFamilyName = getString(cursor, StructuredName.PHONETIC_FAMILY_NAME);
                // if (Build.VERSION.SDK_INT >= 21) {
                //     fullNameStyle = getFullNameStyle(cursor);
                // }
                // phoneticNameStyle = getPhoneticNameStyle(cursor);
            }

            private static String getPhoneticNameStyle(Cursor cursor) {
                switch (getInt(cursor, StructuredName.PHONETIC_NAME_STYLE)) {
                    case ContactsContract.PhoneticNameStyle.UNDEFINED:
                        return "Undefined";
                    case ContactsContract.PhoneticNameStyle.PINYIN:
                        return "Pinyin";
                    case ContactsContract.PhoneticNameStyle.JAPANESE:
                        return "Japanese";
                    case ContactsContract.PhoneticNameStyle.KOREAN:
                        return "Korean";
                    default:
                        return null;
                }
            }


            @TargetApi(21)
            private static String getFullNameStyle(Cursor cursor) {
                if (Build.VERSION.SDK_INT >= 21) {
                    switch (getInt(cursor, StructuredName.FULL_NAME_STYLE)) {
                        case ContactsContract.FullNameStyle.UNDEFINED:
                            return "Undefined";
                        case ContactsContract.FullNameStyle.WESTERN:
                            return "Western";
                        case ContactsContract.FullNameStyle.CJK:
                            return "CJK";
                        case ContactsContract.FullNameStyle.CHINESE:
                            return "Chinese";
                        case ContactsContract.FullNameStyle.JAPANESE:
                            return "Japanese";
                        case ContactsContract.FullNameStyle.KOREAN:
                            return "Korean";
                        default:
                            return null;
                    }
                }
                return null;
            }

            public boolean isValid() {
                return true;
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "displayName", displayName);
                putString(map, "givenName", givenName);
                putString(map, "familyName", familyName);
                putString(map, "prefix", prefix);
                putString(map, "middleName", middleName);
                putString(map, "suffix", suffix);
                putString(map, "phoneticGivenName", phoneticGivenName);
                putString(map, "phoneticMiddleName", phoneticMiddleName);
                putString(map, "phoneticFamilyName", phoneticFamilyName);
                putString(map, "fullNameStyle", fullNameStyle);
                putString(map, "phoneticNameStyle", phoneticNameStyle);
                return map;
            }
        }

        public static class NicknameItem extends BaseInfoItem {
            private String label;
            private String name;

            public NicknameItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                name = getString(cursor, Nickname.NAME);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Nickname.TYPE)) {
                    case Nickname.TYPE_DEFAULT:
                        return "Default";
                    case Nickname.TYPE_OTHER_NAME:
                        return "Other Name";
                    case Nickname.TYPE_MAIDEN_NAME:
                        return "Maiden Name";
                    case Nickname.TYPE_SHORT_NAME:
                        return "Short Name";
                    case Nickname.TYPE_INITIALS:
                        return "Initials";
                    case Nickname.TYPE_CUSTOM:
                        return getString(cursor, Nickname.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(name);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "name", name);
                return map;
            }
        }

        public static class PhoneItem extends BaseInfoItem {
            private String label;
            private String number;
            private String normalizedNumber;

            public PhoneItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                number = getString(cursor, Phone.NUMBER);
                normalizedNumber = getString(cursor, Phone.NORMALIZED_NUMBER);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Phone.TYPE)) {
                    case Phone.TYPE_HOME:
                        return "Home";
                    case Phone.TYPE_MOBILE:
                        return "Mobile";
                    case Phone.TYPE_WORK:
                        return "Work";
                    case Phone.TYPE_FAX_WORK:
                        return "Fax Work";
                    case Phone.TYPE_FAX_HOME:
                        return "Fax Home";
                    case Phone.TYPE_PAGER:
                        return "Pager";
                    case Phone.TYPE_OTHER:
                        return "Other";
                    case Phone.TYPE_CALLBACK:
                        return "Callback";
                    case Phone.TYPE_CAR:
                        return "Car";
                    case Phone.TYPE_COMPANY_MAIN:
                        return "Company Main";
                    case Phone.TYPE_ISDN:
                        return "ISDN";
                    case Phone.TYPE_MAIN:
                        return "Main";
                    case Phone.TYPE_OTHER_FAX:
                        return "Other Fax";
                    case Phone.TYPE_RADIO:
                        return "Radio";
                    case Phone.TYPE_TELEX:
                        return "Telex";
                    case Phone.TYPE_TTY_TDD:
                        return "TTY TDD";
                    case Phone.TYPE_WORK_MOBILE:
                        return "Work Mobile";
                    case Phone.TYPE_WORK_PAGER:
                        return "Work Pager";
                    case Phone.TYPE_ASSISTANT:
                        return "Assistant";
                    case Phone.TYPE_MMS:
                        return "MMS";
                    case Phone.TYPE_CUSTOM:
                        return getString(cursor, Phone.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(number);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "number", number);
                putString(map, "normalizedNumber", normalizedNumber);
                return map;
            }
        }

        public static class EmailItem extends BaseInfoItem {
            private String label;
            private String address;
            private String displayName;

            public EmailItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                address = getString(cursor, Email.ADDRESS);
                displayName = getString(cursor, Email.DISPLAY_NAME);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Email.TYPE)) {
                    case Email.TYPE_HOME:
                        return "Home";
                    case Email.TYPE_WORK:
                        return "Work";
                    case Email.TYPE_OTHER:
                        return "Other";
                    case Email.TYPE_MOBILE:
                        return "Mobile";
                    case Email.TYPE_CUSTOM:
                        return getString(cursor, Email.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(address);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "address", address);
                putString(map, "displayName", displayName);
                return map;
            }
        }

        public static class StructuredPostalItem extends BaseInfoItem {
            private String label;
            private String formattedAddress;
            private String street;
            private String pobox;
            private String neighborhood;
            private String city;
            private String region;
            private String postcode;
            private String country;

            public StructuredPostalItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                formattedAddress = getString(cursor, StructuredPostal.FORMATTED_ADDRESS);
                street = getString(cursor, StructuredPostal.STREET);
                pobox = getString(cursor, StructuredPostal.POBOX);
                neighborhood = getString(cursor, StructuredPostal.NEIGHBORHOOD);
                city = getString(cursor, StructuredPostal.CITY);
                region = getString(cursor, StructuredPostal.REGION);
                postcode = getString(cursor, StructuredPostal.POSTCODE);
                country = getString(cursor, StructuredPostal.COUNTRY);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, StructuredPostal.TYPE)) {
                    case StructuredPostal.TYPE_HOME:
                        return "Home";
                    case StructuredPostal.TYPE_WORK:
                        return "Work";
                    case StructuredPostal.TYPE_OTHER:
                        return "Other";
                    case StructuredPostal.TYPE_CUSTOM:
                        return getString(cursor, StructuredPostal.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return true;
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "formattedAddress", formattedAddress);
                putString(map, "street", street);
                putString(map, "pobox", pobox);
                putString(map, "neighborhood", neighborhood);
                putString(map, "city", city);
                putString(map, "region", region);
                putString(map, "postcode", postcode);
                putString(map, "country", country);
                return map;
            }
        }

        public static class ImItem extends BaseInfoItem {
            private String label;
            private String protocol;
            private String username;

            public ImItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                protocol = getProtocol(cursor);
                username = getString(cursor, Im.DATA);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Im.TYPE)) {
                    case Im.TYPE_HOME:
                        return "Home";
                    case Im.TYPE_WORK:
                        return "Work";
                    case Im.TYPE_OTHER:
                        return "Other";
                    case Im.TYPE_CUSTOM:
                        return getString(cursor, Im.LABEL);
                    default:
                        return null;
                }
            }

            private static String getProtocol(Cursor cursor) {
                switch (getInt(cursor, Im.PROTOCOL)) {
                    case Im.PROTOCOL_CUSTOM:
                        return getString(cursor, Im.CUSTOM_PROTOCOL);
                    case Im.PROTOCOL_AIM:
                        return "AIM";
                    case Im.PROTOCOL_MSN:
                        return "MSN";
                    case Im.PROTOCOL_YAHOO:
                        return "Yahoo";
                    case Im.PROTOCOL_SKYPE:
                        return "Skype";
                    case Im.PROTOCOL_QQ:
                        return "QQ";
                    case Im.PROTOCOL_GOOGLE_TALK:
                        return "Google Talk";
                    case Im.PROTOCOL_ICQ:
                        return "ICQ";
                    case Im.PROTOCOL_JABBER:
                        return "Jabber";
                    case Im.PROTOCOL_NETMEETING:
                        return "NetMeeting";
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(protocol) && !isEmptyString(username);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "protocol", protocol);
                putString(map, "username", username);
                return map;
            }
        }

        public static class OrganizationItem extends BaseInfoItem {
            private String label;
            private String company;
            private String title;
            private String department;
            private String jobDescription;
            private String symbol;
            private String phoneticName;
            private String officeLocation;
            private String phoneticNameStyle;

            public OrganizationItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                company = getString(cursor, Organization.COMPANY);
                title = getString(cursor, Organization.TITLE);
                department = getString(cursor, Organization.DEPARTMENT);
                jobDescription = getString(cursor, Organization.JOB_DESCRIPTION);
                symbol = getString(cursor, Organization.SYMBOL);
                phoneticName = getString(cursor, Organization.PHONETIC_NAME);
                officeLocation = getString(cursor, Organization.OFFICE_LOCATION);
                // phoneticNameStyle = getPhoneticNameStyle(cursor);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Organization.TYPE)) {
                    case Organization.TYPE_WORK:
                        return "Work";
                    case Organization.TYPE_OTHER:
                        return "Other";
                    case Organization.TYPE_CUSTOM:
                        return getString(cursor, Organization.LABEL);
                    default:
                        return null;
                }
            }

            private static String getPhoneticNameStyle(Cursor cursor) {
                switch (getInt(cursor, Organization.PHONETIC_NAME_STYLE)) {
                    case ContactsContract.PhoneticNameStyle.UNDEFINED:
                        return "Undefined";
                    case ContactsContract.PhoneticNameStyle.PINYIN:
                        return "Pinyin";
                    case ContactsContract.PhoneticNameStyle.JAPANESE:
                        return "Japanese";
                    case ContactsContract.PhoneticNameStyle.KOREAN:
                        return "Korean";
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return true;
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "company", company);
                putString(map, "title", title);
                putString(map, "department", department);
                putString(map, "jobDescription", jobDescription);
                putString(map, "symbol", symbol);
                putString(map, "phoneticName", phoneticName);
                putString(map, "officeLocation", officeLocation);
                putString(map, "phoneticNameStyle", phoneticNameStyle);
                return map;
            }
        }

        public static class RelationItem extends BaseInfoItem {
            private String label;
            private String name;

            public RelationItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                name = getString(cursor, Relation.NAME);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Relation.TYPE)) {
                    case Relation.TYPE_ASSISTANT:
                        return "Assistant";
                    case Relation.TYPE_BROTHER:
                        return "Brother";
                    case Relation.TYPE_CHILD:
                        return "Child";
                    case Relation.TYPE_DOMESTIC_PARTNER:
                        return "Domestic Partner";
                    case Relation.TYPE_FATHER:
                        return "Father";
                    case Relation.TYPE_FRIEND:
                        return "Friend";
                    case Relation.TYPE_MANAGER:
                        return "Manager";
                    case Relation.TYPE_MOTHER:
                        return "Mother";
                    case Relation.TYPE_PARENT:
                        return "Parent";
                    case Relation.TYPE_PARTNER:
                        return "Partner";
                    case Relation.TYPE_REFERRED_BY:
                        return "Referred By";
                    case Relation.TYPE_RELATIVE:
                        return "Relative";
                    case Relation.TYPE_SISTER:
                        return "Sister";
                    case Relation.TYPE_SPOUSE:
                        return "Spouse";
                    case Relation.TYPE_CUSTOM:
                        return getString(cursor, Relation.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(name);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "name", name);
                return map;
            }
        }

        public static class EventItem extends BaseInfoItem {
            private String label;
            private String startDate;

            public EventItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                startDate = getString(cursor, Event.START_DATE);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Event.TYPE)) {
                    case Event.TYPE_ANNIVERSARY:
                        return "Anniversary";
                    case Event.TYPE_OTHER:
                        return "Other";
                    case Event.TYPE_BIRTHDAY:
                        return "Birthday";
                    case Event.TYPE_CUSTOM:
                        return getString(cursor, Event.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(startDate);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "startDate", startDate);
                return map;
            }
        }

        public static class NoteItem extends BaseInfoItem {
            private String note;

            public NoteItem(Cursor cursor) {
                super(cursor);
                note = getString(cursor, Note.NOTE);
            }

            public boolean isValid() {
                return !isEmptyString(note);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "note", note);
                return map;
            }
        }

        public static class WebsiteItem extends BaseInfoItem {
            private String label;
            private String url;

            public WebsiteItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                url = getString(cursor, Website.URL);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, Website.TYPE)) {
                    case Website.TYPE_HOMEPAGE:
                        return "Homepage";
                    case Website.TYPE_BLOG:
                        return "Blog";
                    case Website.TYPE_PROFILE:
                        return "Profile";
                    case Website.TYPE_HOME:
                        return "Home";
                    case Website.TYPE_WORK:
                        return "Work";
                    case Website.TYPE_FTP:
                        return "FTP";
                    case Website.TYPE_OTHER:
                        return "Other";
                    case Website.TYPE_CUSTOM:
                        return getString(cursor, Website.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(url);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "url", url);
                return map;
            }
        }

        public static class SipAddressItem extends BaseInfoItem {
            private String label;
            private String sipAddress;

            public SipAddressItem(Cursor cursor) {
                super(cursor);
                label = getLabel(cursor);
                sipAddress = getString(cursor, SipAddress.SIP_ADDRESS);
            }

            private static String getLabel(Cursor cursor) {
                switch (getInt(cursor, SipAddress.TYPE)) {
                    case SipAddress.TYPE_HOME:
                        return "Home";
                    case SipAddress.TYPE_WORK:
                        return "Work";
                    case SipAddress.TYPE_OTHER:
                        return "Other";
                    case SipAddress.TYPE_CUSTOM:
                        return getString(cursor, SipAddress.LABEL);
                    default:
                        return null;
                }
            }

            public boolean isValid() {
                return !isEmptyString(sipAddress);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "label", label);
                putString(map, "sipAddress", sipAddress);
                return map;
            }
        }

        public static class IdentityItem extends BaseInfoItem {
            private String identity;
            private String namespace;

            public IdentityItem(Cursor cursor) {
                super(cursor);
                identity = getString(cursor, Identity.IDENTITY);
                namespace = getString(cursor, Identity.NAMESPACE);
            }

            public boolean isValid() {
                return !isEmptyString(identity) || !isEmptyString(namespace);
            }

            public WritableMap toMap() {
                WritableMap map = super.toMap();
                putString(map, "identity", identity);
                putString(map, "namespace", namespace);
                return map;
            }
        }
    }
}
