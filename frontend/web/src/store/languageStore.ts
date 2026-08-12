import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { translations, LanguageCode } from '../constants/translations';

interface LanguageState {
  language: LanguageCode;
  t: any;
  setLanguage: (lang: LanguageCode) => void;
}

export const useLanguageStore = create<LanguageState>()(
  persist(
    (set) => ({
      language: 'EN',
      t: translations['EN'],
      setLanguage: (lang: LanguageCode) => set({
        language: lang,
        t: translations[lang]
      }),
    }),
    {
      name: 'agriconnect-language',
    }
  )
);
