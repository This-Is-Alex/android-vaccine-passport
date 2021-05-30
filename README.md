# android-vaccine-passport
Android app for decentralised vaccine passport concept

Developed by Alex, Rebekah and Taran

Example barcodes are included in this git repository.

The data format we have used to create these is:
- Date: 4 byte integer, represents number of days since 1/1/1970
- Vaccine type: 1 byte (vaccine type enum TBD)
- Dosage number: 1 byte
- Passport number: 9 bytes (string)
- Passport Expiry Date: 4 byte integer, represents number of days since 1/1/1970
- Date of birth: 4 byte integer, represents number of days since 1/1/1970
- Country: ISO 3-length country string. (3 bytes)
- Name:
    - 1 byte: length of the following string (max 128)
    - Name (string)
Dr Administered:
    - 1 byte: length of the following string (max 128)
    - Name (string)

Here's an example of the data we would expect:
\0x00\0x00\0x49\0x43\0x01\0x01LM696469\0x00\0x00\0x00\0x52\0xEA\0x00\0x00\0x2A\0xF4NZL\0x0BAlex Hobson\0x0EDr John Taylor

If you would like to create your own barcodes you can do so at https://www.free-barcode-generator.net/aztec/

Ensure the settings for your barcode creation are:
- Type of information: Text
- Convert substrings \\, \r, \n, \t, \0xHH to character equivalent (HH is hexadecimal character code in range from 00 to FF): Yes
- Error correction level: 30%
- Output charater code page for characters with codes greater than 127 (characters outside selected code page will be ignored): ISO-8859-1 (Latin 1)
- Dot Size: 9
- Safe margin: 2
- Bars colour: Standard (black)
- Background colour: Standard (white)


# We hope you find Vaccine Passport useful as you travel after the COVID-19 pandemic!