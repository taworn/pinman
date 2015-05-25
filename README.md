# pinman
PIN edit -- an editor designed to edit PIN.


Capabilities
------------

PinEdit (extends EditText) is adds width, height, character drawable and hiding text (use only drawable).


Limitations
-----------

PinEdit has bug in gravity.  Used getGravityFix() and setGravityFix() instead.


Support
-------

Minimum API: 14


Usage
-----

Places PinEdit.java and PinEdit_attrs.xml in your project.  Adds layout file here:

	<diy.pinman.PinEdit 		
		android:id="@+id/edit_pin" 		
		android:layout_width="match_parent" 		
		android:layout_height="wrap_content" 		
		android:gravity="center" 		
		android:inputType="numberPassword" 		
		android:textAppearance="?android:attr/textAppearanceLarge" 		
		custom:charDrawable="@drawable/i" 		
		custom:typedCharDrawable="@drawable/you"/>

And in Java, adds:

	pinEdit = (PinEdit) findViewById(R.id.edit_pin);
	pinEdit.setOnCompleteListener(new PinEdit.OnCompleteListener() {
		@Override
		public void onComplete(String text) {
			if (text.equals("5555")) {
				Toast.makeText(getApplicationContext(), String.format("%s ^_^", text), Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getApplicationContext(), String.format("%s T_T", text), Toast.LENGTH_SHORT).show();
				pinEdit.clear();
			}
		}
	});


API
---

**Gets and sets character width and height.**

	custom:charWidth="..."
	custom:charHeight="..."

	int getCharWidth();
	int getCharHeight();
	void setCharSize(int width, int height);

**Gets and sets character drawable.**

	custom:charDrawable="@drawable/..."

	Drawable getCharDrawable();
	void setCharDrawable(Drawable value);

**Gets and sets typed character drawable.**

	custom:typedCharDrawable="@drawable/..."

	Drawable getTypedCharDrawable();
	void setTypedCharDrawable(Drawable value);

**Gets and sets space between characters.**

	custom:spaceBetweenChars="..."

	int getSpaceBetweenChars();
	void setSpaceBetweenChars(int value);

**Gets and sets hiding characters.**

	custom:hideChars="..."

	boolean getHideChars();
	void setHideChars(boolean value);

**Gets and sets gravity (for bug fix).**

	android:gravity="..."

	int getGravityFix();
	public void setGravityFix(int value);


Last Word
---------

Sorry everyone, but my English is poorly T_T.  Thank you. :)

