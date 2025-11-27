import 'package:flutter/material.dart';
import '../database/database_helper.dart';
import '../models/hike.dart';
import 'hike_detail_screen.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final DatabaseHelper _dbHelper = DatabaseHelper.instance;
  final _simpleSearchController = TextEditingController();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  final _distanceController = TextEditingController();
  final _dateController = TextEditingController();

  bool _isSimpleSearch = true;
  List<Hike> _searchResults = [];
  bool _hasSearched = false;
  bool _isSearching = false;

  @override
  void dispose() {
    _simpleSearchController.dispose();
    _nameController.dispose();
    _locationController.dispose();
    _distanceController.dispose();
    _dateController.dispose();
    super.dispose();
  }

  Future<void> _performSimpleSearch() async {
    final keyword = _simpleSearchController.text.trim();

    if (keyword.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a search term')),
      );
      return;
    }

    setState(() => _isSearching = true);

    final results = await _dbHelper.searchHikesByName(keyword);

    setState(() {
      _searchResults = results;
      _hasSearched = true;
      _isSearching = false;
    });

    if (results.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No results found')),
        );
      }
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${results.length} result(s) found')),
        );
      }
    }
  }

  Future<void> _performAdvancedSearch() async {
    final name = _nameController.text.trim();
    final location = _locationController.text.trim();
    final distance = _distanceController.text.trim();
    final date = _dateController.text.trim();

    if (name.isEmpty && location.isEmpty && distance.isEmpty && date.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter at least one search criteria')),
      );
      return;
    }

    setState(() => _isSearching = true);

    final results = await _dbHelper.advancedSearch(
      name: name.isEmpty ? null : name,
      location: location.isEmpty ? null : location,
      distance: distance.isEmpty ? null : distance,
      date: date.isEmpty ? null : date,
    );

    setState(() {
      _searchResults = results;
      _hasSearched = true;
      _isSearching = false;
    });

    if (results.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No results found')),
        );
      }
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('${results.length} result(s) found')),
        );
      }
    }
  }

  void _clearResults() {
    setState(() {
      _searchResults = [];
      _hasSearched = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Search Hikes'),
      ),
      body: Column(
        children: [
          // Search Type Toggle
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: SegmentedButton<bool>(
                    segments: const [
                      ButtonSegment(
                        value: true,
                        label: Text('Simple'),
                        icon: Icon(Icons.search),
                      ),
                      ButtonSegment(
                        value: false,
                        label: Text('Advanced'),
                        icon: Icon(Icons.filter_list),
                      ),
                    ],
                    selected: {_isSimpleSearch},
                    onSelectionChanged: (Set<bool> newSelection) {
                      setState(() {
                        _isSimpleSearch = newSelection.first;
                        _clearResults();
                      });
                    },
                  ),
                ),
              ],
            ),
          ),

          // Search Form
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  if (_isSimpleSearch) ...[
                    // Simple Search
                    TextField(
                      controller: _simpleSearchController,
                      decoration: const InputDecoration(
                        labelText: 'Search by name',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.search),
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _isSearching ? null : _performSimpleSearch,
                      icon: _isSearching
                          ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                          : const Icon(Icons.search),
                      label: Text(_isSearching ? 'Searching...' : 'Search'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.all(16),
                        minimumSize: const Size(double.infinity, 50),
                      ),
                    ),
                  ] else ...[
                    // Advanced Search
                    TextField(
                      controller: _nameController,
                      decoration: const InputDecoration(
                        labelText: 'Name',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.title),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _locationController,
                      decoration: const InputDecoration(
                        labelText: 'Location',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.location_on),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _distanceController,
                      decoration: const InputDecoration(
                        labelText: 'Minimum Length (km)',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.straighten),
                      ),
                      keyboardType: TextInputType.number,
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _dateController,
                      decoration: const InputDecoration(
                        labelText: 'Date (dd/MM/yyyy)',
                        border: OutlineInputBorder(),
                        prefixIcon: Icon(Icons.calendar_today),
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _isSearching ? null : _performAdvancedSearch,
                      icon: _isSearching
                          ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                          : const Icon(Icons.search),
                      label: Text(_isSearching ? 'Searching...' : 'Search'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.all(16),
                        minimumSize: const Size(double.infinity, 50),
                      ),
                    ),
                  ],

                  const SizedBox(height: 24),

                  // Search Results
                  if (_hasSearched) ...[
                    const Divider(),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Results (${_searchResults.length})',
                          style: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        if (_searchResults.isNotEmpty)
                          TextButton(
                            onPressed: _clearResults,
                            child: const Text('Clear'),
                          ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (_searchResults.isEmpty)
                      Center(
                        child: Padding(
                          padding: const EdgeInsets.all(32),
                          child: Column(
                            children: [
                              Icon(
                                Icons.search_off,
                                size: 64,
                                color: Colors.grey[400],
                              ),
                              const SizedBox(height: 16),
                              Text(
                                'No results found',
                                style: TextStyle(
                                  fontSize: 16,
                                  color: Colors.grey[600],
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                    else
                      ListView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: _searchResults.length,
                        itemBuilder: (context, index) {
                          final hike = _searchResults[index];
                          return Card(
                            elevation: 2,
                            margin: const EdgeInsets.only(bottom: 8),
                            child: ListTile(
                              contentPadding: const EdgeInsets.all(12),
                              leading: CircleAvatar(
                                backgroundColor:
                                _getDifficultyColor(hike.difficulty),
                                child: Text(
                                  hike.difficulty[0].toUpperCase(),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                              title: Text(
                                hike.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const SizedBox(height: 4),
                                  Text(hike.location),
                                  Text(hike.date),
                                ],
                              ),
                              trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                              onTap: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) =>
                                        HikeDetailScreen(hike: hike),
                                  ),
                                );
                              },
                            ),
                          );
                        },
                      ),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Color _getDifficultyColor(String difficulty) {
    switch (difficulty.toLowerCase()) {
      case 'easy':
        return Colors.green;
      case 'medium':
        return Colors.orange;
      case 'hard':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }
}
