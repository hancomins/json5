package com.hancomins.json5.options;

public interface INumberConversionOption {
     boolean isAllowNaN();
     boolean isAllowInfinity();
     boolean isAllowHexadecimal();
     boolean isLeadingZeroOmission();
     boolean isAllowPositiveSing();
     boolean isIgnoreNonNumeric();

     INumberConversionOption DEFAULT_NUMBER_CONVERSIONgetION =    new INumberConversionOption() {
          @Override
          public boolean isAllowNaN() {
               return true;
          }

          @Override
          public boolean isAllowInfinity() {
               return true;
          }

          @Override
          public boolean isAllowHexadecimal() {
               return true;
          }

          @Override
          public boolean isLeadingZeroOmission() {
               return true;
          }

          @Override
          public boolean isAllowPositiveSing() {
               return true;
          }

          @Override
          public boolean isIgnoreNonNumeric() {
               return true;
          }
     };

}