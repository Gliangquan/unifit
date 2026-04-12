#!/usr/bin/env python3
"""
Web scraper for calorietech.com blog content
"""

import requests
from bs4 import BeautifulSoup
import json
import time
from datetime import datetime
from pathlib import Path
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class CalorieTechScraper:
    def __init__(self):
        self.base_url = "https://www.calorietech.com/blog/plan"
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
        }
        self.output_dir = Path(__file__).parent
        
    def fetch_page(self):
        """Fetch the webpage"""
        try:
            logger.info(f"Fetching {self.base_url}...")
            response = requests.get(self.base_url, headers=self.headers, timeout=10)
            response.raise_for_status()
            logger.info(f"Status code: {response.status_code}")
            return response.text
        except requests.RequestException as e:
            logger.error(f"Error fetching page: {e}")
            return None
    
    def parse_content(self, html):
        """Parse HTML content"""
        soup = BeautifulSoup(html, 'html.parser')
        
        data = {
            'url': self.base_url,
            'timestamp': datetime.now().isoformat(),
            'title': None,
            'content': None,
            'articles': []
        }
        
        # Extract page title
        title_tag = soup.find('h1')
        if title_tag:
            data['title'] = title_tag.get_text(strip=True)
        
        # Extract main content
        main_content = soup.find('main') or soup.find('article') or soup.find('div', class_='content')
        if main_content:
            data['content'] = main_content.get_text(strip=True)[:500]  # First 500 chars
        
        # Extract article links and titles
        articles = soup.find_all('article')
        if not articles:
            articles = soup.find_all('div', class_=['post', 'article-item', 'blog-post'])
        
        for article in articles[:10]:  # Limit to 10 articles
            article_data = {}
            
            # Try to find title
            title = article.find(['h2', 'h3', 'a'])
            if title:
                article_data['title'] = title.get_text(strip=True)
            
            # Try to find link
            link = article.find('a', href=True)
            if link:
                article_data['url'] = link['href']
            
            # Try to find excerpt
            excerpt = article.find(['p', 'div'], class_=['excerpt', 'summary', 'description'])
            if excerpt:
                article_data['excerpt'] = excerpt.get_text(strip=True)[:200]
            
            if article_data:
                data['articles'].append(article_data)
        
        return data
    
    def save_results(self, data, html):
        """Save results to files"""
        # Save JSON
        json_file = self.output_dir / 'content.json'
        with open(json_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        logger.info(f"Saved JSON to {json_file}")
        
        # Save HTML
        html_file = self.output_dir / 'page.html'
        with open(html_file, 'w', encoding='utf-8') as f:
            f.write(html)
        logger.info(f"Saved HTML to {html_file}")
        
        # Save summary
        summary_file = self.output_dir / 'summary.txt'
        with open(summary_file, 'w', encoding='utf-8') as f:
            f.write(f"URL: {data['url']}\n")
            f.write(f"Timestamp: {data['timestamp']}\n")
            f.write(f"Title: {data['title']}\n")
            f.write(f"Articles found: {len(data['articles'])}\n\n")
            for i, article in enumerate(data['articles'], 1):
                f.write(f"{i}. {article.get('title', 'N/A')}\n")
                if 'url' in article:
                    f.write(f"   URL: {article['url']}\n")
                if 'excerpt' in article:
                    f.write(f"   Excerpt: {article['excerpt']}\n")
                f.write("\n")
        logger.info(f"Saved summary to {summary_file}")
    
    def run(self):
        """Run the scraper"""
        html = self.fetch_page()
        if not html:
            return False
        
        time.sleep(1)  # Be respectful to the server
        
        data = self.parse_content(html)
        self.save_results(data, html)
        
        logger.info("Scraping completed successfully!")
        return True

if __name__ == '__main__':
    scraper = CalorieTechScraper()
    scraper.run()
